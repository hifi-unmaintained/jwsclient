jwsclient
=========

Minimally *Java Web Start* compatible launcher for .exe wrapping with *Launch4j* for Windows. The sole idea is to avoid the crazy security popups from *javaws* when using real JWS and to improve the launching experience (by making it faster and seamless) while still preserving everything from the JWS chain unchanged and usable as they are.

Personal studies have shown people dislike big security dialogs with warnings and instead love running random executables  without hesitation.

The current implementation focuses on background updates and actually working offline launching. For some reason Sun/Oracle never got them right and the real JWS launcher is a piece of garbage and chokes when offline even when the specification has an offline option.

Usage: `java -jar jwsclient.jar <URL to .jnlp>`

In real world, Launch4j wrap it with static URL as the sole parameter.
