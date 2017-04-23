package ru.javaops.masterjava.export;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import ru.javaops.masterjava.export.PayloadImporter.FailedEmail;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.dao.UserGroupDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.persist.model.UserGroup;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * gkislin
 * 14.10.2016
 */
@Slf4j
public class UserImporter {

    private static final int NUMBER_THREADS = 4;
    private final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);
    private final UserDao userDao = DBIProvider.getDao(UserDao.class);
    private final UserGroupDao userGroupDao = DBIProvider.getDao(UserGroupDao.class);

    public List<FailedEmail> process(StaxStreamProcessor processor, Map<String, City> cities, Map<String, Group> groups, int chunkSize) throws XMLStreamException {
        log.info("Start proseccing with chunkSize=" + chunkSize);

        return new Callable<List<FailedEmail>>() {
            class ChunkFuture {
                String emailRange;
                Future<List<String>> future;

                public ChunkFuture(List<User> chunk, Future<List<String>> future) {
                    this.future = future;
                    this.emailRange = chunk.get(0).getEmail();
                    if (chunk.size() > 1) {
                        this.emailRange += '-' + chunk.get(chunk.size() - 1).getEmail();
                    }
                }
            }

            @Override
            public List<FailedEmail> call() throws XMLStreamException {
                List<ChunkFuture> futures = new ArrayList<>();

                int id = userDao.getSeqAndSkip(chunkSize);
                List<User> chunk = new ArrayList<>(chunkSize);
                List<FailedEmail> failed = new ArrayList<>();
				Map<User, Set<Group>> userWithGroups = new HashMap<>();

                while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
                    final String email = processor.getAttribute("email");
                    String cityRef = processor.getAttribute("city");
                    City city = cities.get(cityRef);
					String groupRefs = processor.getAttribute("groupRefs");
					Set<String> userGroups = groupRefs != null ? Sets.newHashSet(Splitter.on(' ').split(groupRefs)) : Collections.emptySet();
					Sets.SetView<String> difference = Sets.difference(userGroups, groups.keySet());
					if (city == null) {
                        failed.add(new FailedEmail(email, "City '" + cityRef + "' is not present in DB"));
                    } else if (!difference.isEmpty()) {
						failed.add(new FailedEmail(email, "The following groups of user '" + difference + "' are not present in DB"));
					} else {
						final UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
						final String fullName = processor.getReader().getElementText();
						final User user = new User(id++, fullName, email, flag, city.getId());
						if (!userGroups.isEmpty()) {
							userWithGroups.put(user, userGroups.stream().map(groups::get).filter(Objects::nonNull).collect(Collectors.toSet()));
						}
						chunk.add(user);
						if (chunk.size() == chunkSize) {
							futures.add(submit(chunk));
							chunk = new ArrayList<>(chunkSize);
							id = userDao.getSeqAndSkip(chunkSize);
						}
					}
                }

                if (!chunk.isEmpty()) {
                    futures.add(submit(chunk));
                }

                futures.forEach(cf -> {
                    try {
                        failed.addAll(StreamEx.of(cf.future.get()).map(email -> new FailedEmail(email, "already present")).toList());
                        log.info(cf.emailRange + " successfully executed");
                    } catch (Exception e) {
                        log.error(cf.emailRange + " failed", e);
                        failed.add(new FailedEmail(cf.emailRange, e.toString()));
                    }
                });

                Set<String> failedEmails = failed.stream().map(FailedEmail::getEmailOrRange).collect(Collectors.toSet());
				List<UserGroup> forSaving = userWithGroups.entrySet().stream()
					.filter(entry -> !failedEmails.contains(entry.getKey().getEmail()))
					.flatMap(entry -> {
						User user = entry.getKey();
						return entry.getValue().stream()
							.map(group -> new UserGroup(user.getId(), group.getId()));
					})
					.collect(Collectors.toList());

				if (!forSaving.isEmpty()) {
					log.info("Batch insert UserGroups: " + forSaving);
					userGroupDao.insertBatch(forSaving);
				}

				return failed;
            }

            private ChunkFuture submit(List<User> chunk) {
                ChunkFuture chunkFuture = new ChunkFuture(chunk,
                        executorService.submit(() -> userDao.insertAndGetConflictEmails(chunk))
                );
                log.info("Submit " + chunkFuture.emailRange);
                return chunkFuture;
            }
        }.call();
    }
}
