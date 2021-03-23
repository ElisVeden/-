import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.*;
import java.security.AlgorithmParameters;
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

public class RegistrationForm {
    public JPanel Panel;
    private JButton buttonRegistration;
    private JTextField textFieldFIO;
    private JTextField textFieldLogin;
    private JTextField textFieldPassword;
    private JTextArea textAreaErorrs;
    private JButton buttonBack;
    public static JFrame frame;

    public RegistrationForm() {
        buttonRegistration.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //проверка имени на корректность
                String fio = textFieldFIO.getText();
                if (fio.length() < 2) {
                    textAreaErorrs.setBackground(new Color(69, 73, 74));
                    textAreaErorrs.setText("Некорректное имя. \nИмя должено содержать больше 2 символов.\n");
                    return;
                } else {
                    for (int i = 0; i < fio.length(); i++) {
                        if (!(fio.charAt(i) >= 'а' && fio.charAt(i) <= 'я') && !(fio.charAt(i) >= 'А' && fio.charAt(i) <= 'Я')) {
                            textAreaErorrs.setBackground(new Color(69, 73, 74));
                            textAreaErorrs.setText("В имени содержатся недопустимые символы. \nВ имени можно использовать: \nЛюбые буквы русского алфавита (а-я, А-Я)\n");
                            return;
                        }
                    }
                }
                //проверка логина на корректность
                String login = textFieldLogin.getText();
                if (login.length() < 5) {
                    textAreaErorrs.setBackground(new Color(69, 73, 74));
                    textAreaErorrs.setText("Некорректный логин. \nЛогин должен содержать больше 5 символов.\n");
                    return;

                } else {
                    for (int i = 0; i < login.length(); i++) {
                        if (!(login.charAt(i) >= 'a' && login.charAt(i) <= 'z') && !(login.charAt(i) >= 'A' && login.charAt(i) <= 'Z') && !(login.charAt(i) >= '1' && login.charAt(i) <= '9')) {
                            textAreaErorrs.setBackground(new Color(69, 73, 74));
                            textAreaErorrs.setText("В логине содержатся недопустимые символы. \nВ логине можно использовать: \nЛюбые латинские буквы (a-z, A-Z), Любые цифры (0-9)\n");
                            return;
                        }
                    }
                }
                //проверка пароля на корректность
                String password = textFieldPassword.getText();
                if (password.length() < 7) {
                    textAreaErorrs.setBackground(new Color(69, 73, 74));
                    textAreaErorrs.setText("Некорректный пароль. \nПароль должен содержать не меньше 8 символов.\n");
                    return;
                }


                //проверка существования УЗ
                try {
                    File file = new File("Users.txt");
                    FileReader fr = new FileReader(file);
                    BufferedReader reader = new BufferedReader(fr);
                    String line = reader.readLine();
                    String[] userData;
                    while (line != null) {
                        userData = line.split(String.valueOf(' '));
                        if (userData[1].equals(login)) {
                            textAreaErorrs.setBackground(new Color(69, 73, 74));
                            textAreaErorrs.setText("Указанный логин уже существует в системе.\nВведите другой логин или авторизуйтесь под текущим");
                            fr.close();
                            return;
                        }
                        line = reader.readLine();
                    }
                } catch (FileNotFoundException ef) {
                    ef.printStackTrace();
                } catch (IOException ex) {

                    System.out.println(ex.getMessage());
                }

                //Добавление новой УЗ в файл
                try (FileWriter writer = new FileWriter("Users.txt", true)) {
                    // запись всей строки
                    writer.write(fio);
                    writer.append(" ");
                    writer.write(login);
                    writer.append(" ");
                    writer.write(encryptPassword(password));
                    writer.append("\n");
                } catch (IOException | GeneralSecurityException ex) {

                    System.out.println(ex.getMessage());
                }

                //Добавление новой статистики в файл
                try (FileWriter writer = new FileWriter("UserStats.txt", true)) {
                    // запись всей строки
                    writer.write(login);
                    writer.append(" ");
                    writer.write("0.00");
                    writer.append(" ");
                    writer.write("0.00");
                    writer.append(" ");
                    writer.write("0.00");
                    writer.append("\n");
                } catch (IOException ex) {

                    System.out.println(ex.getMessage());
                }

                textAreaErorrs.setBackground(new Color(69, 73, 74));
                textAreaErorrs.setText("Учётная запись зарегистрирована.");
                buttonRegistration.setEnabled(false);
            }
        });

        textFieldLogin.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                focus();
            }
        });
        textFieldPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                focus();
            }
        });


        textFieldFIO.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                focus();
            }
        });
        buttonBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.frame.setVisible(true);
                Main.reg_frame.setVisible(false);
            }
        });
    }

    private void focus() {
        if(!textAreaErorrs.getText().equals("Учётная запись зарегистрирована.")) {
            textAreaErorrs.setText("");
            textAreaErorrs.setBackground(new Color(0, 153, 153));
        }
    }

    private String encryptPassword(String originalPassword) throws GeneralSecurityException, UnsupportedEncodingException {
        byte[] salt = new String("12345678").getBytes();
        int iterationCount = 40000;
        int keyLength = 128;
        SecretKeySpec key = createSecretKey(originalPassword.toCharArray(), salt, iterationCount, keyLength);
        return encrypt(originalPassword, key);
    }

    private static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey keyTmp = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }

    private static String encrypt(String property, SecretKeySpec key) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
        AlgorithmParameters parameters = pbeCipher.getParameters();
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
        byte[] cryptoText = pbeCipher.doFinal(property.getBytes("UTF-8"));
        byte[] iv = ivParameterSpec.getIV();
        return base64Encode(iv) + ":" + base64Encode(cryptoText);
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
