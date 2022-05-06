/**
 * This class provides a GUI for our river object, and contains methods used for crossing rivers. The player can pay
 * $20 to take a ferry, build a raft using 2 wagon tools, or attempt to swim for free, albeit with great risk.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RiverGUI extends JDialog {
    private JPanel contentPane;
    public JTextField inputText;
    private JTextArea promptTextArea;
    public JLabel riverImage;
    private JLabel promptLabel;
    private JButton buttonOK;
    private OregonTrailGUI game;
    private Wagon wagon;
    private Location location;

    public RiverGUI(Location location, OregonTrailGUI game) {
        this.game = game;
        this.location = location;
        this.wagon = game.getWagon();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        inputText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                crossRiver();
            }
        });
    }

    /**
     * Sets the input text box to a blank String.
     */
    private void resetTextBox() {
        inputText.setText("");

    }

    /**
     * Allows the user to cross the river. The player can take a ferry across for $20, build a raft using 2 tools, or
     * attempt to swim across.
     */
    public void crossRiver() {
        crossChoice = inputText.getText();
        switch (crossChoice) {
            case "1" -> {
                if (takeFerry()) {
                    dispose();
                } else {
                    resetTextBox();
                }
            }
            case "2" -> {
                if (buildRaft()) {
                    dispose();
                } else {
                    resetTextBox();
                }
            }
            case "3" -> {
                if (crossAlone()) {
                    dispose();
                } else {
                    resetTextBox();
                }
            }
            default -> {
                staticMethods.notValidInput();
                resetTextBox();
            }
        }
    }

    /**
     * Sets the text message to be displayed in the prompt.
     * @param message - Message to be set in the prompt.
     */
    public void setText(String message) {
        promptTextArea.setText(message);
    }
    String crossChoice;

    /**
     * Gets the text from the prompt.
     */
    public void getText() {
        location.setRiverChoice(inputText.getText());
        dispose();
    }

    //Booleans: true = crossed; false = didn't. (Still true if someone gets hurt/sick/etc but crossed successfully)

    /**
     * The player can take the ferry across the river for $20 if they have enough money.
     * @return true if the ferry crosses the river, false, if it does not.
     */
    public boolean takeFerry() {
        if (game.getMoney() >= 20) {
            game.setMoney(game.getMoney() - 20);
            return true;
        }
        else {
            staticMethods.notEnoughMoney();
        }
        return false;
    }


    /**
     * The player can build a raft using 2 wagon tools to cross the river. There is a 10% chance 1 oxen dies, a 15%
     * chance someone gets sick along the way, and a 5% chance for wagon damage.
     * @return true if the raft crosses the river, false if it does not.
     */
    public boolean buildRaft() {
        if (game.getWagonTools() >= 2) {
            game.setWagonTools(game.getWagonTools() - 2);

            //10% 1 oxen dies
            if (game.rand.nextInt(9) == 0) {
                game.setOxen(game.getOxen() - 1);
                if (game.getOxen() == 0) {
                    staticMethods.cantCross("All your oxen died while crossing.\nYou can't continue your journey.");
                    game.checkIfLost();
                    return false;
                }
            }

            //15% for someone to get sick
            if (game.rand.nextInt(99) <= 15) {
                int characterIndex = game.rand.nextInt(game.characterArrayList.size());
                game.characterArrayList.get(characterIndex).setSick(true);
            }

            //5% wagon damage
            if (game.rand.nextInt(20) == 0) {
                wagon.setState(wagon.getState() + 1);
                if (wagon.getState() == 2) {
                    game.checkIfLost();
                }
            }
            return true;
        }
        else {
            staticMethods.notEnoughItem("WAGON TOOLS");
            return false;
        }
    }

    /**
     * Attempt to cross the river without a medium (i.e., by swimming). There is a 10% chance 1 oxen dies or a 20%
     * chance 2 oxen die, a 5% chance someone drowns, a 15% chance someone gets sick, and a 10% chance the wagon breaks.
     * @return true if the party makes it across the river, false if all oxen die causing everyone to drown.
     */
    public boolean crossAlone() {
        //10% chance 1 oxen dies
        if (game.rand.nextInt(9) == 0 && game.getOxen() > 0) {
            game.setOxen(game.getOxen() - 1);
            if (game.getOxen() <= 0) {
                game.checkIfLost();
                staticMethods.cantCross("All your oxen died while crossing. Everyone drowned.");
                return false;
            }
        }
        //or 20% chance 2 oxen die
        else if (game.rand.nextInt(4) == 0) {
            if (game.getOxen() == 1) {
                game.setOxen(game.getOxen() - 1);
            }
            else {
                game.setOxen(game.getOxen() - 2);
            }
            if (game.getOxen() <= 0) {
                game.checkIfLost();
                staticMethods.cantCross("All your oxen died while crossing. Everyone drowned.");
                return false;
            }
        }

        //5% chance someone drowns
        if (game.rand.nextInt(19) == 0) {
            boolean someoneDrowns = false;
            int characterIndex;
            do {
                characterIndex = game.rand.nextInt(game.characterArrayList.size());
                if (!game.characterArrayList.get(characterIndex).getIsDead()) {
                    game.characterArrayList.get(characterIndex).setIsDead(true);
                    someoneDrowns = true;
                    game.checkIfLost();
                }
            } while (!someoneDrowns);
        }

        //15% chance someone gets sick
        if (game.rand.nextInt(99) <= 15) {
            int characterIndex = game.rand.nextInt(game.characterArrayList.size());
            game.characterArrayList.get(characterIndex).setSick(true);
        }

        //10% chance wagon breaks
        if (game.rand.nextInt(10) == 0) {
            wagon.setState(wagon.getState() + 1);
            game.checkIfLost();
        }
        return true;
    }
}
