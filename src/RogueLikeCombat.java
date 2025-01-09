import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import javax.swing.*;

public class RogueLikeCombat extends JPanel implements KeyListener {
    private final RogueLikeGame game;
    private int playerHealth;

    private final Enemy enemy;
    private int enemyHealth;
    private int enemyDmg;

    private boolean playerRolled = false;
    private boolean enemyRolled = false;
    private int playerRoll = 0;
    private int enemyRoll = 0;
    private final Random random = new Random();

    public RogueLikeCombat(RogueLikeGame game, int playerHealth, Enemy enemy) {
        this.game = game;
        this.playerHealth = playerHealth;
        
        this.enemy = enemy;
        this.enemyHealth = enemy.getVita();
        this.enemyDmg = enemy.getDanni();

        setPreferredSize(new Dimension(400, 400));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
    }

    private int rollDice() {
        return random.nextInt(6) + 1;
    }

    private void resolveCombat() {
        boolean playerWon = enemyHealth <= 0;
        game.endCombat(playerWon, playerHealth, enemy);
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.GREEN);
        g.fillRect(50, 150, 100, 100); // Enemy square

        g.setColor(Color.MAGENTA);
        g.fillRect(250, 150, 100, 100); // Player square

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        g.drawString("Enemy: " + enemyHealth, 50, 100);
        g.drawString("Player: " + playerHealth, 250, 100);

        if (enemyRolled) {
            g.drawString(String.valueOf(enemyRoll), 90, 200);
        }

        if (playerRolled) {
            g.drawString(String.valueOf(playerRoll), 290, 200);
        }

        if (!playerRolled || !enemyRolled) {
            g.drawString("SPACEBAR", 100, 300);
        } else {
            g.drawString("SPACEBAR", 100, 300);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!playerRolled || !enemyRolled) {
                if (!playerRolled) {
                    playerRoll = rollDice();
                    playerRolled = true;
                } else if (!enemyRolled) {
                    enemyRoll = rollDice();
                    enemyRolled = true;
                }
                repaint();
            } else {
                // Apply damage
                if (playerRoll > enemyRoll) {
                    enemyHealth--;
                } else if (enemyRoll > playerRoll) {
                    playerHealth--;
                }

                // Reset rolls for next round
                playerRolled = false;
                enemyRolled = false;

                // Check if combat ends
                if (enemyHealth <= 0 || playerHealth <= 0) {
                    resolveCombat();
                }

                repaint();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }
}