/**
 * @author - Deepasree Meena Padmanabhan 
 * @studentID - 239490480
 * @version - 1.0
 */

package com.carrental.controllers;

import com.carrental.DatabaseConnection;
import com.carrental.SceneManager;
import com.carrental.controllers.auth.LoginController;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.testfx.framework.junit5.ApplicationTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginControllerTest extends ApplicationTest {

	private LoginController controller;
	private Connection mockConn;
	private PreparedStatement mockStmt;
	private ResultSet mockRs;
	private MockedStatic<DatabaseConnection> staticDB;

	@Override
	public void start(Stage stage) {
	}

	@BeforeEach
	void setUp() throws Exception {
		controller = new LoginController();
		controller.usernameField = new TextField();
		controller.passwordField = new PasswordField();
		controller.userErrorLabel = new Label();
		controller.passwordErrorLabel = new Label();
		controller.logoImage = new ImageView();
		controller.userErrorLabel.setVisible(false);
		controller.passwordErrorLabel.setVisible(false);

		mockConn = mock(Connection.class);
		mockStmt = mock(PreparedStatement.class);
		mockRs = mock(ResultSet.class);

		when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
		when(mockStmt.executeQuery()).thenReturn(mockRs);

		staticDB = mockStatic(DatabaseConnection.class);
		staticDB.when(DatabaseConnection::getConnection).thenReturn(mockConn);
	}

	@AfterEach
	void tearDown() {
		staticDB.close();
	}

	@Test
	void testInitializeLogoImage() {
		Platform.runLater(() -> controller.initialize());
	}

	@Test
	void testHandleLoginWithInvalidCredentials() throws Exception {
		controller.usernameField.setText("wronguser");
		controller.passwordField.setText("wrongpass");

		when(mockRs.next()).thenReturn(false);

		CountDownLatch latch = new CountDownLatch(1);
		Platform.runLater(() -> {
			controller.handleLogin();
			latch.countDown();
		});
		latch.await();

		assertTrue(controller.userErrorLabel.isVisible());
		assertTrue(controller.passwordErrorLabel.isVisible());
	}

	@Test
	void testHandleLoginWithUnknownRole() throws Exception {
		controller.usernameField.setText("validuser");
		controller.passwordField.setText("validpass");

		when(mockRs.next()).thenReturn(true);
		when(mockRs.getString("password")).thenReturn(controller.hashPassword("validpass"));
		when(mockRs.getInt("user_id")).thenReturn(999);
		when(mockRs.getString("role")).thenReturn("Alien");
		when(mockRs.getString("email")).thenReturn("alien@mock.com");
		when(mockRs.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));

		CountDownLatch latch = new CountDownLatch(1);
		Platform.runLater(() -> {
			controller.handleLogin();
			latch.countDown();
		});
		latch.await();

		assertTrue(controller.userErrorLabel.isVisible());
	}

	@Test
	void testGoToResetPasswordNavigation() {
		try (MockedStatic<SceneManager> mockedScene = mockStatic(SceneManager.class)) {
			controller.goToResetPassword();
			mockedScene.verify(() -> SceneManager.showScene("resetpassword"));
		}
	}

	@Test
	void testGoToSignupNavigation() {
		try (MockedStatic<SceneManager> mockedScene = mockStatic(SceneManager.class)) {
			controller.goToSignup();
			mockedScene.verify(() -> SceneManager.showScene("signup"));
		}
	}

	@Test
	void testGoToPolicyNavigation() {
		try (MockedStatic<SceneManager> mockedScene = mockStatic(SceneManager.class)) {
			controller.goToPolicy();
			mockedScene.verify(() -> SceneManager.showScene("policy"));
		}
	}

	@Test
	void testHashPasswordConsistency() {
		String password = "MySecurePassword@123";
		String hashed = controller.hashPassword(password);
		assertEquals(hashed, controller.hashPassword(password));
	}

	@Test
	void testHandleLoginWithWrongPassword() throws Exception {
		controller.usernameField.setText("admin");
		controller.passwordField.setText("wrongpass");

		when(mockRs.next()).thenReturn(true);
		when(mockRs.getString("password")).thenReturn(controller.hashPassword("correctpass"));
		when(mockRs.getInt("user_id")).thenReturn(102);
		when(mockRs.getString("role")).thenReturn("Admin");
		when(mockRs.getString("email")).thenReturn("admin@example.com");
		when(mockRs.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));

		CountDownLatch latch = new CountDownLatch(1);
		Platform.runLater(() -> {
			controller.handleLogin();
			latch.countDown();
		});
		latch.await();

		assertTrue(controller.userErrorLabel.isVisible());
		assertTrue(controller.passwordErrorLabel.isVisible());
	}
}
