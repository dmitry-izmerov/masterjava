package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * gkislin
 * 27.10.2016
 * <p>
 * <p>
 */
@RegisterMapperFactory(EntityMapperFactory.class)
@UseStringTemplate3StatementLocator
public abstract class UserDao implements AbstractDao {

	private static final Logger LOG = LoggerFactory.getLogger(UserDao.class);

	private static final int BATCH_CHUNK_SIZE = 10;

	private final ExecutorService executorService = Executors.newFixedThreadPool(8);

	public ConcurrentUserSaver getUserSaver() {
		return new ConcurrentUserSaver();
	}

    public User insert(User user) {
        if (user.isNew()) {
            int id = insertGeneratedId(user);
            user.setId(id);
        } else {
            insertWitId(user);
        }
        return user;
    }

	public void insertInBatchMode(List<User> users, Integer chunkSize) {
		List<String> fullNames = users.stream().map(User::getFullName).collect(Collectors.toList());
		List<String> emails = users.stream().map(User::getEmail).collect(Collectors.toList());
		List<UserFlag> flags = users.stream().map(User::getFlag).collect(Collectors.toList());
		batchInsert(fullNames, emails, flags, chunkSize != null ? chunkSize : BATCH_CHUNK_SIZE);
	}

    @SqlUpdate("INSERT INTO users (full_name, email, flag) VALUES (:fullName, :email, CAST(:flag AS user_flag)) ON CONFLICT (email) DO NOTHING")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean User user);

    @SqlUpdate("INSERT INTO users (id, full_name, email, flag) VALUES (:id, :fullName, :email, CAST(:flag AS user_flag)) ON CONFLICT (email) DO NOTHING")
    abstract void insertWitId(@BindBean User user);

	@SqlBatch("INSERT INTO users (full_name, email, flag) VALUES (:fullName, :email, CAST(:userFlag AS user_flag)) ON CONFLICT (email) DO NOTHING")
	public abstract void batchInsert(@Bind("fullName") List<String> fullNames, @Bind("email") List<String> emails, @Bind("userFlag") List<UserFlag> userFlags, @BatchChunkSize Integer chunkSize);

    @SqlQuery("SELECT * FROM users ORDER BY full_name, email LIMIT :it")
    public abstract List<User> getWithLimit(@Bind int limit);

	@SqlQuery("SELECT * FROM users ORDER BY <orderParam> <sortParam> LIMIT :limit")
	public abstract List<User> getAllByOrderWithLimit(@Define("orderParam") String orderParam, @Define("sortParam") String sortParam, @Bind("limit") int limit);

    //   http://stackoverflow.com/questions/13223820/postgresql-delete-all-content
    @SqlUpdate("TRUNCATE users")
    @Override
    public abstract void clean();

	public class ConcurrentUserSaver {

		private static final int NUM_USERS_FOR_INSERT_AT_TIME = 2;
		private static final String INTERRUPTED_BY_TIMEOUT = "Thread was interrupted by timeout";
		private static final String INTERRUPTED_EXCEPTION = "There was happened InterruptedException";

		private int chunkSize = BATCH_CHUNK_SIZE;

		private List<User> users = new ArrayList<>(NUM_USERS_FOR_INSERT_AT_TIME);

		private final CompletionService<UsersResult> completionService = new ExecutorCompletionService<>(executorService);

		private List<Future<UsersResult>> futures = new ArrayList<>();

		public void save(User user) {
			users.add(user);
			if (users.size() != NUM_USERS_FOR_INSERT_AT_TIME) {
				return;
			}
			LinkedList<User> usersQueue = new LinkedList<>(users);
			futures.add(completionService.submit(() -> saveUsers(usersQueue)));
			users.clear();
		}

		/**
		 * Save the rest of objects from users and clear list
		 */
		public void flush() {
			if (users.isEmpty()) {
				return;
			}
			LinkedList<User> usersQueue = new LinkedList<>(users);
			futures.add(completionService.submit(() -> saveUsers(usersQueue)));
			users.clear();
		}

		private UsersResult saveUsers(Deque<User> users) {

			if (users.isEmpty()) {
				return UsersResult.error(users, "List of users was empty.");
			}

			UserDao newUserDao = null;
			try {
				newUserDao = DBIProvider.getDBI().open(UserDao.class);
				newUserDao.insertInBatchMode(new ArrayList<>(users), chunkSize);
			} catch (Exception e) {
				LOG.error("Saving of {} users was failed", users, e);
				String message;
				User firstUser = users.getFirst();
				if (users.size() > 1) {
					User lastUser = users.getLast();
					message = String.format("User saving is failed from user with email %s to user with email %s.", firstUser.getEmail(), lastUser.getEmail());
				} else {
					message = String.format("User saving is failed for user with email %s.", firstUser.getEmail());
				}
				return UsersResult.error(users, message);
			} finally {
				if (newUserDao != null) {
					DBIProvider.getDBI().close(newUserDao);
				}
			}
			return UsersResult.ok(users);
		}

		public SavingResults getFeedback() {
			return new Callable<SavingResults>() {
				private List<UsersResult> results = new ArrayList<>();

				@Override
				public SavingResults call() {
					while (!futures.isEmpty()) {
						try {
							Future<UsersResult> future = completionService.poll(10, TimeUnit.SECONDS);
							if (future == null) {
								return cancelWithFail(INTERRUPTED_BY_TIMEOUT);
							}
							futures.remove(future);
							results.add(future.get());
						} catch (ExecutionException e) {
							return cancelWithFail(e.getCause().toString());
						} catch (InterruptedException e) {
							return cancelWithFail(INTERRUPTED_EXCEPTION);
						}
					}

					return new SavingResults(results, null);
				}

				private SavingResults cancelWithFail(String cause) {
					futures.forEach(f -> f.cancel(true));
					return new SavingResults(results, cause);
				}
			}.call();
		}

		public void setChunkSize(int chunkSize) {
			this.chunkSize = chunkSize;
		}
	}

	public static class UsersResult {
		private static final String OK = "Users were saved successfully.";

		private final Deque<User> users;
		private final String result;

		private static UsersResult ok(Deque<User> users) {
			return new UsersResult(users, OK);
		}

		private static UsersResult error(Deque<User> users, String error) {
			return new UsersResult(users, error);
		}

		public boolean isOk() {
			return OK.equals(result);
		}

		private UsersResult(Deque<User> users, String result) {
			this.users = users;
			this.result = result;
		}

		public Deque<User> getUsers() {
			return users;
		}

		public String getResult() {
			return result;
		}

		@Override
		public String toString() {
			return "UsersResult{" +
				"users=" + users +
				", result='" + result + '\'' +
				'}';
		}
	}

	public static class SavingResults {

		private final List<UsersResult> results;
		private final String failedCause;  // global fail cause

		public SavingResults(List<UsersResult> results, String failedCause) {
			this.results = results;
			this.failedCause = failedCause;
		}

		public List<UsersResult> getResults() {
			return results;
		}

		public String getFailedCause() {
			return failedCause;
		}

		@Override
		public String toString() {
			return "Results: " + results.toString() + '\n' +
				(failedCause == null ? "" : "Failed cause" + failedCause);
		}
	}
}
