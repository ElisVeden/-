import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Main {
    private JTextField textFieldlogin;
    private JButton войтиButton;
    private JPasswordField textFieldpassword;
    private JButton регистрацияButton;
    public JPanel Panel;
    private JLabel mainlabel;
    private JTextField textFieldErrors;
    public static JFrame frame;
    public static JFrame reg_frame;
    private String[] StatsData;

    public Main() {

        регистрацияButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reg_frame = new JFrame("RegistrationForm");
                reg_frame.setContentPane(new RegistrationForm().Panel);
                reg_frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                reg_frame.pack();
                reg_frame.setVisible(true);

                frame.setVisible(false);
            }
        });


        войтиButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String login = textFieldlogin.getText(); //считывание логина
                String password = textFieldpassword.getText(); //считывание пароля
                String decPassword = "";
                BufferedReader lreader = null;
                //авторизация
                try {
                    File loginfile = new File("Users.txt");
                    FileReader lfr = new FileReader(loginfile);
                    //создаем BufferedReader с существующего FileReader для построчного считывания
                    lreader = new BufferedReader(lfr);
                    // считаем сначала первую строку
                    String line = lreader.readLine();
                    String[] userData;

                    //цикл считывания логинов и паролей
                    while (line != null) {
                        userData = line.split(String.valueOf(' '));
                        String userName = userData[0];
                        if (login.equals(userData[1])) {
                            byte[] salt = new String("12345678").getBytes();
                            int iterationCount = 40000;
                            int keyLength = 128;
                            SecretKeySpec key = createSecretKey(password.toCharArray(), salt, iterationCount, keyLength);
                            decPassword = decrypt(userData[2], key);
                            JFrame wo_frame;
                            WorkOutForm wof = new WorkOutForm();
                            wo_frame = new JFrame("WorkOutForm");
                            wo_frame.setContentPane(wof.Panel);
                            String[] finalUserData = userData;
                            /////////////

                            //чтение данных о соженных каллориях из файла по логину
                            File statsfile = new File("UserStats.txt");
                            BufferedReader sreader = null;
                            try {
                                FileReader sfr = new FileReader(statsfile);
                                sreader = new BufferedReader(sfr);
                                // считаем сначала первую строку
                                String sline = sreader.readLine();
                                while (sline != null) {
                                    StatsData = sline.split(String.valueOf(' '));
                                    if (login.equals(StatsData[0])) {
                                        break;
                                    }
                                    sline = sreader.readLine();
                                }
                                sfr.close();
                            } catch (FileNotFoundException fileNotFoundException) {
                                fileNotFoundException.printStackTrace();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            } finally {
                                sreader.close();
                            }
                            /////////////
                            //вывод считанной информации по каллориям на форму тренировок
                            WindowListener windowOpened = new WindowAdapter() {
                                @Override
                                public void windowOpened(WindowEvent e) {
                                    wof.labeluserName.setText(wof.labeluserName.getText() + userName);
                                    wof.textAreakkal.setText("Отжимания = " + StatsData[1] + "\n" + "Скакалка = "
                                            + StatsData[2] + "\n" + "Приседания = " + StatsData[3] + "\n");
                                    wof.login = login;
                                    super.windowOpened(e);
                                }
                            };
                            wo_frame.addWindowListener(windowOpened);
                            wo_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            wo_frame.pack();
                            wo_frame.setVisible(true);
                            wo_frame.setSize(500, 500);
                            return;
                        }
                        line = lreader.readLine();
                    }
                    textFieldErrors.setBackground(new Color(69, 73, 74));
                    textFieldErrors.setText("Логин или пароль неверный");
                } catch (FileNotFoundException ef) {
                    ef.printStackTrace();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                } catch (GeneralSecurityException generalSecurityException) {
                    textFieldErrors.setBackground(new Color(69, 73, 74));
                    textFieldErrors.setText("Логин или пароль неверный");
                    return;
                } finally {
                    try {
                        lreader.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });
        textFieldlogin.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                focus();
            }
        });
        textFieldpassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                focus();
            }
        });
    }

    private void focus() {
        textFieldErrors.setText("");
        textFieldErrors.setBackground(new Color(92, 204, 204));
    }

    public static void main(String[] args) {
        frame = new JFrame("Main");
        frame.setContentPane(new Main().Panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey keyTmp = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }

    private static String decrypt(String string, SecretKeySpec key) throws GeneralSecurityException, IOException {
        String iv = string.split(":")[0];
        String property = string.split(":")[1];
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(iv)));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }
}
