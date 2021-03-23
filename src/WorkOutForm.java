import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class WorkOutForm {
    public JPanel Panel;
    private JList list1;
    private JButton buttonstart;
    private JButton buttonstop;
    public JTextArea textAreakkal;
    public JLabel labeluserName;
    private JTextArea textAreatimer;
    private JTextField textFieldErrors;
    private JTextArea textAreakkalnow;
    long startTime;
    Date now;
    public String login;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private Timer ClockTimer = new Timer(500, new Clock());

    public WorkOutForm() {

        buttonstart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textFieldErrors.setText("");
                if (list1.getSelectedIndex() == -1) {
                    textFieldErrors.setText("Необходимо выбрать вид тренировки");
                } else {
                    startTime = System.nanoTime();
                    now = new Date();
                    textAreatimer.setText("Время начала: " + dateFormat.format(now) + "\n");
                    ClockTimer.start();
                    buttonstop.setEnabled(true);
                    buttonstart.setEnabled(false);
                }
            }
        });

        buttonstop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textFieldErrors.setText("");
                long stopTime = System.nanoTime();
                textAreatimer.setText("Время начала: " + dateFormat.format(now) + "\n");
                textAreatimer.setText(textAreatimer.getText() + "Время окончания: " + dateFormat.format(new Date()) + "\n");
                textAreatimer.setText(textAreatimer.getText() + "Длительность тренировки: " + TimeUnit.NANOSECONDS.toSeconds(stopTime - startTime) + " сек. \n");
                ClockTimer.stop();
                buttonstop.setEnabled(false);
                buttonstart.setEnabled(true);

                double k1 = 0, k2 = 0, k3 = 0;
                DecimalFormat f = new DecimalFormat("##.00");
                //расчёт сжигания каллорий
                switch (list1.getSelectedIndex()) {
                    case 0:
                        k1 = 0.05 * TimeUnit.NANOSECONDS.toSeconds(stopTime - startTime);
                        k1 = (double) Math.round(k1 * 100) / 100;
                        textAreakkalnow.setText("Сожжено калорий: " + k1);
                        break;
                    case 1:
                        k2 = 0.23 * TimeUnit.NANOSECONDS.toSeconds(stopTime - startTime);
                        k2 = (double) Math.round(k2 * 100) / 100;
                        textAreakkalnow.setText("Сожжено калорий: " + k2);
                        break;
                    case 2:
                        k3 = 0.23 * TimeUnit.NANOSECONDS.toSeconds(stopTime - startTime);
                        k3 = (double) Math.round(k3 * 100) / 100;
                        textAreakkalnow.setText("Сожжено калорий: " + k3);
                        break;
                    default:
                        textAreakkalnow.setText("Произошла ошибка");
                }
                BufferedReader reader = null;
                BufferedWriter writer = null;
                //обновление статистики в файле
                try {
                    File file = new File("UserStats.txt");
                    File file2 = new File("UserStats2.txt");
                    FileReader fr = new FileReader(file);
                    reader = new BufferedReader(fr);
                    writer = new BufferedWriter(new FileWriter(file2));
                    String line = reader.readLine();
                    String[] userData;
                    while (line != null) {
                        userData = line.split(String.valueOf(' '));
                        if (userData[0].equals(login)) {
                            k1 = Double.parseDouble(userData[1]) + k1;
                            k2 = Double.parseDouble(userData[2]) + k2;
                            k3 = Double.parseDouble(userData[3]) + k3;
                            writer.write(userData[0] + " " + k1 + " " + k2 + " " + k3 + "\n");
                            textAreakkal.setText("Отжимания = " + k1 + "\n" + "Скакалка = " + k2 + "\n" + "Приседания = " + k3 + "\n");
                        } else {
                            writer.write(line + "\n");
                        }
                        line = reader.readLine();
                    }

                    reader.close();
                    writer.close();
                    file.delete();
                    file2.renameTo(file);
                } catch (FileNotFoundException ef) {
                    ef.printStackTrace();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                } finally {
                    try {
                        reader.close();
                        writer.close();
                    } catch (IOException ioException) {
                        System.out.println(ioException.getMessage());
                    }

                }
            }
        });

        list1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                textFieldErrors.setText("");
            }
        });
    }

    //класс для отображения длительности тренировки в настоящее время
    class Clock implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            long time = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()) - TimeUnit.NANOSECONDS.toSeconds(startTime);
            textAreatimer.setText("Время начала: " + dateFormat.format(now) + "\n");
            textAreatimer.setText(textAreatimer.getText() + "Длительность тренировки: " + Objects.toString(time, null) + " сек.");
        }
    }
}
