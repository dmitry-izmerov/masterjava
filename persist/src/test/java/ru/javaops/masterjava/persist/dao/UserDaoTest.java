package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.UserTestData;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.javaops.masterjava.persist.UserTestData.BATCH_USERS;
import static ru.javaops.masterjava.persist.UserTestData.FIST5_USERS;
import static ru.javaops.masterjava.persist.UserTestData.LAST4_USERS_IN_REVERT_ORDER;

/**
 * gkislin
 * 27.10.2016
 */
public class UserDaoTest extends AbstractDaoTest<UserDao> {

    public UserDaoTest() {
        super(UserDao.class);
    }

    @BeforeClass
    public static void init() throws Exception {
        UserTestData.init();
    }

    @Before
    public void setUp() throws Exception {
        UserTestData.setUp();
    }

    @Test
    public void getWithLimit() {
        List<User> users = dao.getWithLimit(5);
        Assert.assertEquals(FIST5_USERS, users);
    }

    @Test
	public void shouldReturnLast4Users() {
    	// given

		// when
		List<User> users = dao.getAllByOrderWithLimit("id", "DESC", 4);

		//then
		Assert.assertEquals(LAST4_USERS_IN_REVERT_ORDER, users);
	}

    @Test
	public void shouldInsertUsersInBatchMode() {
    	// given
		List<String> fullNames = BATCH_USERS.stream().map(User::getFullName).collect(Collectors.toList());
		List<String> emails = BATCH_USERS.stream().map(User::getEmail).collect(Collectors.toList());
		List<UserFlag> userFlags = BATCH_USERS.stream().map(User::getFlag).collect(Collectors.toList());
		List<User> batchUsers = new ArrayList<>(BATCH_USERS);
		Collections.reverse(batchUsers);

		// when
		dao.batchInsert(fullNames, emails, userFlags, 10);

		// then
		List<User> users = dao.getAllByOrderWithLimit("id", "DESC", 3).stream()
			.map(user -> {
				user.setId(null);
				return user;
			})
			.collect(Collectors.toList());
		Assert.assertEquals(batchUsers, users);
	}
}