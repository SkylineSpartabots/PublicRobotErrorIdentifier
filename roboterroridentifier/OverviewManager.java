package roboterroridentifier;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import roboterroridentifier.LoggerFilter.LogList;

/**
 * A class to manage overviews.
 */
public class OverviewManager {
    private static JFrame sliderFrame;
    private static JPanel jp;
    private static JSlider sliderBar;
    private static JLabel jlb;
    private static JTextArea jta;
    private static JComboBox<Object> jcb;
    private static int tValue;
    private static LogList mData;
    private static ArrayList<String> activeActuators = new ArrayList<>();
    private static JLabel pointLabel = new JLabel("Last clicked point: X, Y");

    public static void createSliderWindow(final LogList data) {
        mData = data;
        sliderFrame = new JFrame("Slider Frame");
        jp = new JPanel();
        jp.setLayout(new FlowLayout());
        sliderBar = new JSlider(0, GraphManager.maxSec(data), 0);
        jlb = new JLabel();
        final SliderListener s = new SliderListener();
        sliderBar.addChangeListener(s);
        final String[] SUBSYSTEM_KEYS_EXTENDED = new String[LoggerFilter.SUBSYSTEM_KEYS.length + 1];
        SUBSYSTEM_KEYS_EXTENDED[0] = "All";
        for (int i = 1; i < SUBSYSTEM_KEYS_EXTENDED.length; i++) {
            SUBSYSTEM_KEYS_EXTENDED[i] = LoggerFilter.SUBSYSTEM_KEYS[i - 1];
        }
        jcb = new JComboBox<>(SUBSYSTEM_KEYS_EXTENDED);

        sliderBar.setBounds(50, 25, 200, 50);
        sliderBar.setPaintTrack(true);
        sliderBar.setPaintTicks(true);
        sliderBar.setPaintLabels(true);
        sliderBar.setMajorTickSpacing(25);
        sliderBar.setMinorTickSpacing(5);
        sliderBar.setBackground(LoggerGUI.spartaGreen);
        sliderBar.setForeground(LoggerGUI.plainWhite);

        jlb.setBounds(275, 25, 100, 50);
        jlb.setText("@t = " + sliderBar.getValue());

        jcb.setBounds(50, 125, 300, 20);
        jcb.setBackground(LoggerGUI.spartaGreen);
        jcb.setForeground(LoggerGUI.plainWhite);

        jta = new JTextArea();
        final JScrollPane tlviewer = new JScrollPane(jta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        tlviewer.setBounds(0, 150, 400, 400);

        jcb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent a) {
                jlb.setText("@t = " + sliderBar.getValue() + " - " + (sliderBar.getValue() + 1));
                updateErrors(sliderBar.getValue());
            }
        });
        pointLabel.setBounds(125, 75, 400, 50);
        sliderFrame.add(sliderBar);
        sliderFrame.add(jlb);
        sliderFrame.add(jcb);
        sliderFrame.add(tlviewer);
        sliderFrame.add(pointLabel);
        sliderFrame.add(jp);
        sliderFrame.setSize(400, 600);
        sliderFrame.setResizable(false);
        sliderFrame.setVisible(true);
    }

    public static int getTValue() {
        return tValue;
    }

    public static void updateErrors(final int t) {
        activeActuators.clear();
        final LogList timedLog = new LogList();
        final int bottomBound = t;
        final int topBound = t + 1;
        for (int j = 0; j < mData.timeStamps.size(); j++) {
            final double ts = Double.parseDouble(mData.timeStamps.get(j));
            if (ts > bottomBound && ts <= topBound) {
                timedLog.messages.add(mData.messages.get(j));
                timedLog.timeStamps.add(mData.timeStamps.get(j));
            }
        }
        final ArrayList<LogList> subLogs = new ArrayList<>();
        for (int i = 0; i < LoggerFilter.SUBSYSTEM_KEYS.length; i++) {
            subLogs.add(new LogList());
        }
        for (int i = 0; i < timedLog.messages.size(); i++) {
            for (int j = 0; j < LoggerFilter.SUBSYSTEM_KEYS.length; j++) {
                if (timedLog.messages.get(i).contains(LoggerFilter.SUBSYSTEM_KEYS[j])) {
                    subLogs.get(j).messages.add(timedLog.messages.get(i));
                    subLogs.get(j).timeStamps.add(timedLog.timeStamps.get(i));
                }
            }
        }
        jta.setText("");
        for (int i = 0; i < LoggerFilter.SUBSYSTEM_KEYS.length; i++) {
            if (checkAllowedDisplay(i)) {
                jta.append("Logs in " + LoggerFilter.SUBSYSTEM_KEYS[i] + ":\n");
                for (int j = 0; j < subLogs.get(i).messages.size(); j++) {
                    jta.append(subLogs.get(i).messages.get(j) + " @t = " + subLogs.get(i).timeStamps.get(j) + "\n");
                    for (int k = 0; k < LoggerFilter.ACTUATOR_NAMES.size(); k++) {
                        if (subLogs.get(i).messages.get(j).contains("@" + LoggerFilter.ACTUATOR_NAMES.get(k) + "@")) {
                            if (!activeActuators.contains(LoggerFilter.ACTUATOR_NAMES.get(k))) {
                                activeActuators.add(LoggerFilter.ACTUATOR_NAMES.get(k));
                            }
                        }
                    }
                }
                jta.append("\n");
            }
        }
    }

    public static boolean checkAllowedDisplay(final int n) {
        boolean canDo = false;
        if (jcb.getSelectedItem().toString().equals("All")) {
            canDo = true;
        } else {
            if (jcb.getSelectedItem().toString().equals(LoggerFilter.SUBSYSTEM_KEYS[n])) {
                canDo = true;
            } else {
                canDo = false;
            }
        }
        return canDo;
    }

    public static class SliderListener implements ChangeListener {
        @Override
        public void stateChanged(final ChangeEvent e) {
            jlb.setText("@t = " + sliderBar.getValue() + " - " + (sliderBar.getValue() + 1));
            updateErrors(sliderBar.getValue());
            sliderBar.requestFocusInWindow();
        }
    }
}