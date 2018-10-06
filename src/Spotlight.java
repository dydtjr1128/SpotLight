import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URL;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class Spotlight {

	private Spotlight spotlight = this;
	private Stack<Integer> keyStack = new Stack<Integer>();
	private Robot robot;
	private Clipboard clipboard;

	public Spotlight() {
		makeTray();
		init();
		run();
	}

	public void run() {
		GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

			@Override
			public void nativeKeyTyped(NativeKeyEvent e) {

			}

			@Override
			public void nativeKeyReleased(NativeKeyEvent e) {
				keyStack.push(e.getKeyCode());
				if (keyStack.size() > 1) {
					int a = keyStack.get(0);
					int b = keyStack.get(1);
					int lc = NativeKeyEvent.VC_CONTROL;
					int space = NativeKeyEvent.VC_SPACE;
					if ((a == lc && b == space) || (a == space && b == lc)) {
						new Thread() {
							public void run() {

								robot.keyPress(KeyEvent.VK_CONTROL);
								robot.keyPress(KeyEvent.VK_C);
								robot.keyRelease(KeyEvent.VK_CONTROL);
								robot.keyRelease(KeyEvent.VK_C);
								try {
									sleep(300);
									Transferable contents = clipboard.getContents(clipboard);
									if (contents != null && Desktop.isDesktopSupported()) {
										String pasteString = (String) (contents
												.getTransferData(DataFlavor.stringFlavor));
										pasteString = pasteString.replaceAll("\\+", "%2B");
										pasteString = pasteString.replaceAll(" ", "+");
										pasteString = pasteString.replaceAll("https://", "");
										pasteString = pasteString.replaceAll("http://", "");
										pasteString = pasteString.replaceAll("&", "%26");
										pasteString = pasteString.replaceAll("/", "%2F");
										pasteString = pasteString.replaceAll("=", "%3D");
										pasteString = pasteString.replaceAll("\"", "");
										//System.out.println(pasteString);

										String URL = "http://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query="
												+ pasteString + "";
										Desktop.getDesktop().browse(new URI(URL));
									}
								} catch (Exception e1) {

									e1.printStackTrace();
								}
							}
						}.start();

					}
					keyStack.clear();
				}
			}

			@Override
			public void nativeKeyPressed(NativeKeyEvent e) {
			}
		});
	}

	public void init() {
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		try {
			robot = new Robot();
		} catch (AWTException e2) {
			e2.printStackTrace();
		}
	}

	public void makeTray() {
		URL trayURL = getClass().getClassLoader().getResource("tray.png");
		// ImageIcon trayIcon = new ImageIcon(trayURL);
		MenuItem exititem = new MenuItem("exit");
		PopupMenu menu = new PopupMenu("My Menu");
		menu.add(exititem);
		exititem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(1);
			}
		});
		TrayIcon myTray = new TrayIcon(Toolkit.getDefaultToolkit().getImage(trayURL), "chat", menu);
		SystemTray tray = SystemTray.getSystemTray();
		try {
			tray.add(myTray);
		} catch (AWTException e1) {
			System.out.println(e1.getMessage());
		}
		myTray.setImageAutoSize(true);
	}

	public static void main(String[] args) {
		try {
			if (!GlobalScreen.isNativeHookRegistered()) {
				GlobalScreen.registerNativeHook();
				Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
				logger.setLevel(Level.OFF);
			}

		} catch (NativeHookException e) {

		}
		new Spotlight();
	}
}
