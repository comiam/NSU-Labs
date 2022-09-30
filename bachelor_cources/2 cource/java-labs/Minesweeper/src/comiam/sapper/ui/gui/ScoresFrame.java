package comiam.sapper.ui.gui;

import comiam.sapper.game.records.Pair;
import comiam.sapper.game.records.ScoreRecords;
import comiam.sapper.ui.gui.components.CustomButton;
import comiam.sapper.ui.gui.components.UIDesigner;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ScoresFrame
{
    private static JTextArea textArea = null;

    public static void showRecords(JFrame parent)
    {
        ArrayList<Pair> pairs = ScoreRecords.getRecords();
        if(pairs == null)
        {
            JOptionPane.showMessageDialog(parent, "Score list is empty!");
            return;
        }

        JPanel recPanel = new JPanel();
        recPanel.setLayout(new BorderLayout());
        recPanel.setBackground(UIDesigner.DEFAULT_BACKGROUND);

        JPanel recHPanel = new JPanel();
        recHPanel.setLayout(new BorderLayout());
        recHPanel.setBackground(UIDesigner.DEFAULT_BACKGROUND);
        recHPanel.setMinimumSize(new Dimension(250, 40));
        recHPanel.setPreferredSize(new Dimension(250, 40));

        JLabel recL = new JLabel("Records list:");
        recL.setFont(UIDesigner.getFont(20, recL.getFont(), false));
        recL.setForeground(Color.LIGHT_GRAY);
        recHPanel.add(recL, BorderLayout.WEST);

        CustomButton removeRecords = new CustomButton("Remove all");
        removeRecords.setFont(UIDesigner.getFont(12, removeRecords.getFont(), false));
        removeRecords.addActionListener(e -> {
            ScoreRecords.removeRecords();
            textArea.setText("");
        });
        removeRecords.setAlignmentX(JButton.EAST);
        recHPanel.add(removeRecords, BorderLayout.EAST);

        recPanel.add(recHPanel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        textArea.setBackground(UIDesigner.DEFAULT_BACKGROUND);
        textArea.setForeground(Color.LIGHT_GRAY);

        for(var p : pairs)
            textArea.setText(textArea.getText() + p.getName() + ": " + p.getTime() + "\n");

        JScrollPane scrollPane = new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(250, 300));
        recPanel.add(scrollPane, BorderLayout.CENTER);
        recPanel.setBackground(UIDesigner.DEFAULT_BACKGROUND);
        recPanel.setForeground(Color.LIGHT_GRAY);

        JFrame frame = new JFrame();
        frame.setContentPane(recPanel);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setTitle("Records");
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
