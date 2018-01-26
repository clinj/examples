package de.jmens.clinj;

import static java.text.MessageFormat.format;

import de.jmens.clinj.model.Faxline;
import de.jmens.clinj.model.Phoneline;
import de.jmens.clinj.phone.ClinjPhone;
import de.jmens.clinj.phone.OutboundCall;
import de.jmens.clinj.suite.ClinjSuite;
import io.vavr.control.Try;
import java.io.File;
import java.nio.file.Path;
import java.util.Random;

public class Example {

	private static final String ANY_PHONENUMBER = "0123999111999";
	private static String username = "username";
	private static byte[] password = "password".getBytes();

	private static String sipUsername = "1234567e0";
	private static byte[] sipPassword = "password".getBytes();

	private static Random random = new Random();

	/**
	 * Shows how to obtain ClinjSuite and ClinjPhone objects.
	 * These are core objects to all further functionality.
	 */
	public void instantiateClinjCoreClasses() {

		final ClinjSuite suite = ClinjSuite.getSuite(username, password);

		final ClinjPhone phone = suite.getPhone(sipUsername, sipPassword);

		System.out.println("Instantiated fresh phone " + phone);

		final Phoneline phoneline = suite.getPhonelines().get(0);
		final ClinjPhone anotherPhone = suite.getPhone(phoneline);

		System.out.println("Instantiated another fresh phone " + anotherPhone);
	}

	/**
	 * Shows how to send a short message.
	 */
	public void sendSms() {
		ClinjSuite.getSuite(username, password)
				.sendSms("Hello World", ANY_PHONENUMBER);
	}

	/**
	 * Shows how to send a fax message.
	 */
	public void sendFax() {
		final ClinjSuite suite = ClinjSuite.getSuite(username, password);

		final Faxline faxline = suite.getFaxlines().get(0);
		final Path pdf = getPathFor("demo.pdf");

		suite.sendFax(faxline, ANY_PHONENUMBER, pdf);
	}

	/**
	 * Easiest way to start an outgoing phone call.
	 */
	public void callRemotePhone() throws Exception {
		final String callId = ClinjSuite.getSuite(username, password)
				.getPhone(sipUsername, sipPassword)
				.newCall(ANY_PHONENUMBER)
				.dial()
				.get();

		System.out.println(format("Started call with call id {0}. A phone should start ringing somewhere.", callId));
	}

	/**
	 * Get in touch with outgoing call events.
	 */
	public void getEventsForOutgoingCall()  {
		final OutboundCall call = ClinjSuite.getSuite(username, password)
				.getPhone(sipUsername, sipPassword)
				.newCall(ANY_PHONENUMBER);

		call.remoteRingingFuture().thenAccept(p -> System.out.println("Remote phone is ringing"));
		call.remotePickedUpFuture().thenAccept(p -> System.out.println("Remote party picked up the line"));
		call.remoteHangupFuture().thenAccept(p -> System.out.println("Remote party hung up the line"));
		call.terminationFuture().thenAccept(p -> System.out.println("Call has been terminated"));

		call.dial();
	}

	/**
	 * Shows how to handle incoming calls.
	 */
	public void getIncomingCall() {
		final ClinjPhone phone = ClinjSuite.getSuite(username, password)
				.getPhone(sipUsername, sipPassword);

		phone.addInboundListener(call -> {
			System.out.println(format("Receiving incoming call from {0}", call.getCaller()));

			if (random.nextBoolean()) {
				System.out.println("Picking up call");
				call.pickup();
			} else {
				System.out.println("Don't care about the call");
			}

			pause(5000);

			System.out.println("Terminating call");
			call.hangup();
		});
	}

	private Path getPathFor(String resource) {
		return Try.of(() -> new File(getClass().getResource("").toURI()).toPath())
				.getOrElseThrow(() -> new RuntimeException(format("Resource not found: {0}", resource)));
	}

	private void pause(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// Sad but true...
		}
	}


}
