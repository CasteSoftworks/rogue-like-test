import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

public class RogueLikeGame extends JPanel implements KeyListener {
    /** La larghezza del frame */
    private final int width;
    /** La altezza del frame */
    private final int height;
    /** Il numero di righe della mappa */
    private final int rows;
    /** Il numero di colonne della mappa */
    private final int cols;

    private final int dim=24;

    /** La mappa del gioco */
    private final char[][] map;

    /** Inizializzazione di Random */
    private final Random random = new Random();

    /** La posizione del giocatore */
    private int playerRow, playerCol;
    /** La posizione del portale */
    private int portalRow, portalCol;

    /** La lista degli nemici */
    private List<Enemy> enemies = new ArrayList<>();
    private EnemyManager gestoreNemici;
    private BufferedImage zombieImage;
    private BufferedImage skeletonImage;
    private BufferedImage vampireImage;

    /** La booleana che indica se il gioco è finito */
    private boolean gameOver = false;
    /** La booleana che indica se il gioco è vinto */
    private boolean gameWin = false;
    /** Il livello del gioco */
    private int level = 1;

    /** La salute del giocatore */
    private int playerHealth = 100;

    /** Inizializzazione di Combat */
    private RogueLikeCombat combat;
    /** La booleana che indica se il giocatore è in combattimento */
    @SuppressWarnings("unused")
    private boolean inCombat = false;

    /**
     * Costruttore di RogueLikeGame
     * 
     * @param width la larghezza del frame
     * @param height l'altezza del frame
     */
    public RogueLikeGame(int width, int height) {
        this.width = width;
        this.height = height;
        this.rows =  height/ dim;
        this.cols =  width / dim;

        this.map = new char[rows][cols];

        this.gestoreNemici = new EnemyManager();

        setPreferredSize(new Dimension(cols * dim, rows * dim));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        loadImages();
        generateMap();
        placePlayer();
        placePortal();
        placeEnemies();
    }

    private void loadImages() {
        try {
            zombieImage = ImageIO.read(new File("src/icone/zombie.png"));
            skeletonImage = ImageIO.read(new File("src/icone/scheletro.png"));
            vampireImage = ImageIO.read(new File("src/icone/vampiro.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Errore nel caricamento delle immagini!");
        }
    }

    /**
     * Il metodo per passare al livello successivo
     */
    private void nextLevel() {
        enemies.clear();
        generateMap();
        placePlayer();
        placePortal();
        gestoreNemici.generaNemici(map, level);
        placeEnemies();
        playerHealth = 100; // Ripristina la salute del giocatore
        gameOver = false; // Ripristina lo stato di fine partita
        gameWin = false;  // Ripristina lo stato di vittoria
        level++; // Incrementa il livello
        repaint();
    }

    /**
     * Il metodo per generare la mappa
     */
    private void generateMap() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                map[row][col] = '#';
            }
        }

        List<Rectangle> rooms = new ArrayList<>();
        int roomCount = 8;

        // Genera stanze casuali
        for (int i = 0; i < roomCount; i++) {
            int width = random.nextInt(10 - 5 + 1) + 5;
            int height = random.nextInt(10 - 5 + 1) + 5;
            int x = random.nextInt(cols - width - 1) + 1;
            int y = random.nextInt(rows - height - 1) + 1;

            Rectangle newRoom = new Rectangle(x, y, width, height);
            boolean overlaps = false;

            for (Rectangle room : rooms) {
                if (newRoom.intersects(room)) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                rooms.add(newRoom);
                createRoom(newRoom);
            }
        }

        // Collega le stanze con corridoi
        for (int i = 1; i < rooms.size(); i++) {
            Rectangle prevRoom = rooms.get(i - 1);
            Rectangle currRoom = rooms.get(i);

            int prevCenterX = prevRoom.x + prevRoom.width / 2;
            int prevCenterY = prevRoom.y + prevRoom.height / 2;
            int currCenterX = currRoom.x + currRoom.width / 2;
            int currCenterY = currRoom.y + currRoom.height / 2;

            if (random.nextBoolean()) {
                createHorizontalCorridor(prevCenterX, currCenterX, prevCenterY);
                createVerticalCorridor(prevCenterY, currCenterY, currCenterX);
            } else {
                createVerticalCorridor(prevCenterY, currCenterY, prevCenterX);
                createHorizontalCorridor(prevCenterX, currCenterX, currCenterY);
            }
        }
    }

    /**
     * Il metodo per creare una stanza
     * 
     * @param room
     */
    private void createRoom(Rectangle room) {
        for (int row = room.y; row < room.y + room.height; row++) {
            for (int col = room.x; col < room.x + room.width; col++) {
                map[row][col] = '.';
            }
        }
    }

    /**
     * Il metodo per creare un corridoio orizzontale
     * 
     * @param x1 inizio del corridoio
     * @param x2 fine del corridoio
     * @param y larghezza del corridoio
     */
    private void createHorizontalCorridor(int x1, int x2, int y) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            map[y][x] = '.';
        }
    }

    /**
     * Il metodo per creare un corridoio verticale
     * 
     * @param y1 inizio del corridoio
     * @param y2 fine del corridoio
     * @param x larghezza del corridoio
     */
    private void createVerticalCorridor(int y1, int y2, int x) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            map[y][x] = '.';
        }
    }

    /**
     * Il metodo per posizionare il giocatore
     */
    private void placePlayer() {
        while (true) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            if (map[row][col] == '.') {
                playerRow = row;
                playerCol = col;
                break;
            }
        }
    }

    /**
     * Il metodo per posizionare il portale
     */
    private void placePortal() {
        while (true) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            if (map[row][col] == '.') {
                portalRow = row;
                portalCol = col;
                break;
            }
        }
    }

    /**
     * Il metodo per posizionare i nemici
     */
    private void placeEnemies() {
        enemies=gestoreNemici.generaNemici(map, level);
    }
    

    /**
     * Il metodo per muovere i nemici nella mappa
     */
    private void moveEnemies() {
        for (Enemy enemy : enemies) {
            if (canSeePlayer(enemy)) {
                moveTowardPlayer(enemy);
            } else {
                moveRandomly(enemy);
            }
        }
    }

    /**
     * Il metodo per controllare se un nemico può vedere il giocatore
     * 
     * @param enemy
     * @return true se il nemico può vedere il giocatore, false altrimenti
     */
    private boolean canSeePlayer(Enemy enemy) {
        int enemyRow = enemy.getRow();
        int enemyCol = enemy.getCol();

        // Calcola la distanza euclidea
        int dx = playerCol - enemyCol;
        int dy = playerRow - enemyRow;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 6) {
            return false; // Fuori dal raggio
        }

        // Controlla la linea di vista usando il tracciamento del raggio
        return hasLineOfSight(enemyRow, enemyCol, playerRow, playerCol);
    }

    /**
     * Il metodo per controllare se c'è una linea di vista tra due punti
     * 
     * @param x1 coordinata x del punto 1
     * @param y1 coordinata y del punto 1
     * @param x2 coordinata x del punto 2
     * @param y2 coordinata y del punto 2
     * @return true se c'è una linea di vista, false altrimenti
     */
    private boolean hasLineOfSight(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;

        int err = dx - dy;

        while (true) {
            if (map[x1][y1] == '#') {
                return false; // Muro blocca la vista
            }

            if (x1 == x2 && y1 == y2) {
                return true; // Linea di vista libera
            }

            int e2 = 2 * err;

            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }

            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    /**
     * Il metodo per muovere un nemico verso il giocatore
     * 
     * @param enemy il nemico da muovere
     */
    private void moveTowardPlayer(Enemy enemy) {
        int dx = playerCol - enemy.getCol();
        int dy = playerRow - enemy.getRow();

        if (Math.abs(dx) > Math.abs(dy)) { // Movimento orizzontale
            if (dx > 0 && map[enemy.getRow()][enemy.getCol() + 1] == '.') {
                enemy.updateCol(+1);
            } else if (dx < 0 && map[enemy.getRow()][enemy.getCol() - 1] == '.') {
                enemy.updateCol(-1);
            }
        } else { // Movimento verticale
            if (dy > 0 && map[enemy.getRow() + 1][enemy.getCol()] == '.') {
                enemy.updateRow(+1);
            } else if (dy < 0 && map[enemy.getRow() - 1][enemy.getCol()] == '.') {
                enemy.updateRow(-1);
            }
        }
    }

    /**
     * Il metodo per muovere un nemico casualmente
     * 
     * @param enemy il nemico da muovere
     */
    private void moveRandomly(Enemy enemy) {
        // Movimento casuale in una delle 4 direzioni
        int[] directions = {-1, 1, 0, 0};
        int[] rowOffsets = {0, 0, -1, 1};

        int direction = random.nextInt(4);
        int newRow = enemy.getRow() + directions[direction];
        int newCol = enemy.getCol() + rowOffsets[direction];

        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols && map[newRow][newCol] == '.') {
            enemy.setRow(newRow);
            enemy.setCol(newCol);
        }
    }

    /**
     * Il metodo per controllare se il gioco è finito
     * 
     * @return true se il gioco è finito, false altrimenti
     */
    private boolean checkGameOver() {
        for (Enemy enemy : enemies) {
            if (enemy.getRow() == playerRow && enemy.getCol() == playerCol) {
                startCombat(enemy);
                return false; // Interrompi per il combattimento
            }
        }
        return false;
    }

    /**
     * Il metodo per iniziare il combattimento
     * 
     * @param enemy il nemico con cui combattere
     */
    private void startCombat(Enemy enemy) {
        combat = new RogueLikeCombat(this, playerHealth, enemy);
        inCombat = true;
        JFrame combatFrame = new JFrame("Combat");
        combatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        combatFrame.add(combat);
        combatFrame.pack();
        combatFrame.setLocationRelativeTo(null);
        combatFrame.setVisible(true);
    }

    /**
     * Il metodo per terminare il combattimento
     * 
     * @param playerWon true se il giocatore ha vinto, false altrimenti
     * @param newPlayerHealth la nuova salute del giocatore
     * @param defeatedEnemy il nemico sconfitto
     */
    void endCombat(boolean playerWon, int newPlayerHealth, Enemy defeatedEnemy) {
        playerHealth = newPlayerHealth;
        inCombat = false;

        if (playerWon) {
            enemies.remove(defeatedEnemy);
            gestoreNemici.rimuoviNemico(defeatedEnemy);
        } else {
            gameOver = true;
        }

        repaint();
    }

    /**
     * Il metodo per disegnare il gioco
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);

        Font font = new Font("Monospaced", Font.PLAIN, dim);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);

        // Impostiamo il colore per i muri
        Color wallColor = new Color(64, 64, 64); // Grigio scuro
        Color floorColor = new Color(128, 128, 128); // Grigio

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (map[row][col] == '#') {
                    // Se la cella è un muro, la riempiamo con il colore grigio
                    g.setColor(wallColor);
                    g.fillRect(col * dim, row * dim, dim, dim);
                } else if (row == playerRow && col == playerCol) {
                    // Disegnare il giocatore
                    g.setColor(Color.MAGENTA);
                    g.fillRect(col * dim, row * dim, dim, dim);
                } else if (row == portalRow && col == portalCol) {
                    // Disegnare il portale
                    g.setColor(Color.CYAN);
                    g.fillRect(col * dim, row * dim, dim, dim);
                } else {
                    // Disegnare il pavimento o altri oggetti
                    boolean isEnemy = false;
                    for (Enemy nemico : gestoreNemici.getNemici()) {
                        int nemicoRow = nemico.getRow();
                        int nemicoCol = nemico.getCol();
                        if (nemico.getTipo() == 'Z') {
                            g.drawImage(zombieImage, nemicoCol * dim, nemicoRow * dim, dim, dim, this);
                        } else if (nemico.getTipo() == 'S') {
                            g.drawImage(skeletonImage, nemicoCol * dim, nemicoRow * dim, dim, dim, this);
                        }else if (nemico.getTipo() == 'V') {
                            g.drawImage(vampireImage, nemicoCol * dim, nemicoRow * dim, dim, dim, this);
                        }
                        
                    }
                    if (!isEnemy) {
                        g.setColor(floorColor);
                        g.fillRect(col * dim, row * dim, dim, dim);  // Riempie il pavimento con il colore bianco
                    }
                }
            }
        }

        // Disegno della barra delle informazioni in alto
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, dim));
        g.drawString("Vita: " + playerHealth, 10, 20); // Mostra la vita in alto a sinistra
        String levelText = "Livello: " + level;
        int levelTextWidth = metrics.stringWidth(levelText);
        g.drawString(levelText, width - levelTextWidth - 10, 20); // Mostra il livello in alto a destra considerando la dimensione del testo

        if (gameOver || gameWin) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, cols * dim, rows * dim);

            if (gameOver) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.GREEN);
            }

            g.setFont(new Font("Monospaced", Font.BOLD, 42));
            String message = gameOver ? "GAME OVER - LEVEL "+level : "GAME WIN";
            int x = (cols * dim - 3 * metrics.stringWidth(message)) / 2;
            int y = (rows * dim) / 2;
            g.drawString(message, x, y);
        }
    }

    /**
     * Il metodo per gestire gli eventi di tastiera
     * 
     * @param e l'evento di tastiera
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver || gameWin) return;

        int newRow = playerRow;
        int newCol = playerCol;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> newRow--;
            case KeyEvent.VK_DOWN -> newRow++;
            case KeyEvent.VK_LEFT -> newCol--;
            case KeyEvent.VK_RIGHT -> newCol++;
            case KeyEvent.VK_SPACE -> {
                if (playerRow == portalRow && playerCol == portalCol) {
                    nextLevel(); // Avanza al livello successivo
                    return;
                }else{
                    return;
                }
            }
            case KeyEvent.VK_ESCAPE -> System.exit(0);
        }

        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols && map[newRow][newCol] == '.') {
            playerRow = newRow;
            playerCol = newCol;

            moveEnemies();
            if (checkGameOver()) gameOver = true;
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) { }
    @Override
    public void keyTyped(KeyEvent e) { }

    /**
     * Il metodo per avviare il gioco
     * 
     * @param gamePanel il pannello del gioco
     */
    public void start(RogueLikeGame gamePanel) {
        JFrame frame = new JFrame("RogueLike Game - Giocatore e Nemici");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}