
package locro;



import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.util.ArrayList;

import java.util.Random;



public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {



    private Timer timer;

    private int playerX = 300;

    private final int playerY = 500;

    private final int playerWidth = 64;

    private final int playerHeight = 64;

    private ArrayList<Rectangle> invaders;

    private ArrayList<Rectangle> bullets;

    private ArrayList<Rectangle> enemyBullets;

    private ArrayList<Particle> particles;

    private boolean moveLeft, moveRight;

    private int invaderDirection = 1;

    private int lives = 3;

    private boolean canShoot = false;

    private final int SHOOT_COOLDOWN = 500; // En milisegundos

    private long lastShotTime = 0;

    private Random random;

    private boolean gameOver;

    private boolean levelCompleted;

    private boolean choosingAbility;

    private final int RAY = 1;

    private final int TRIPLE_SHOT = 2;

    private final int HEAL = 3;

    private JButton healButton, speedUpButton, rayButton, tripleShotButton;

    private ArrayList<Integer> selectedAbilities;

    private int level;

    private int enemyHealth = 2; // Vida base de los enemigos (2 golpes)

    private boolean enemiesCanShoot = false   ; // Indica si los enemigos pueden disparar

    private int score = 0; // Contador de puntos

    private final int SPEED_UP = 4;

    private Image playerImage;

    private Image enemyImage;

    private Image backgroundImage;
    
    private int playerSpeed = 10; // Velocidad inicial del jugador

    private boolean playerHit = false;
    private long playerHitTime = 0;

    private long invaderHitTime = 0;
    private int invaderHitIndex = -1; // Índice del invasor impactado


    public SpaceInvaders() {

        // Cargar imágenes

        playerImage = new ImageIcon(getClass().getResource("/imagen/a.jpg")).getImage();

        enemyImage = new ImageIcon(getClass().getResource("/imagen/enemigo.png")).getImage();
      
        backgroundImage = new ImageIcon(getClass().getResource("/imagen/background.gif")).getImage(); // Asegúrate de tener la imagen de fondo



        // El resto de la inicialización

        level = 1;

        selectedAbilities = new ArrayList<>();

        particles = new ArrayList<>();
        setPreferredSize(new Dimension(800, 600));

        setBackground(Color.BLACK);

        setFocusable(true);
        addKeyListener(this);

        setupAbilityButtons();

        startNewLevel();

    }



    private void setupAbilityButtons() {

        setLayout(null); // Permite posicionar botones libremente



        rayButton = new JButton("Ray");

        rayButton.setBounds(250, 350, 100, 50);

        rayButton.setBackground(Color.GREEN);

        rayButton.setForeground(Color.BLACK);

        rayButton.setFocusPainted(false);

        rayButton.setBorderPainted(false);

        rayButton.addActionListener(e -> selectAbility(RAY));



        tripleShotButton = new JButton("Triple Shot");

        tripleShotButton.setBounds(450, 350, 150, 50);

        tripleShotButton.setBackground(Color.YELLOW);

        tripleShotButton.setForeground(Color.BLACK);

        tripleShotButton.setFocusPainted(false);

        tripleShotButton.setBorderPainted(false);

        tripleShotButton.addActionListener(e -> selectAbility(TRIPLE_SHOT));



        healButton = new JButton("Heal");

        healButton.setBounds(250, 450, 100, 50);

        healButton.setBackground(Color.BLUE);

        healButton.setForeground(Color.WHITE);

        healButton.setFocusPainted(false);

        healButton.setBorderPainted(false);

        healButton.addActionListener(e -> selectAbility(HEAL));



        speedUpButton = new JButton("Speed Up");

        speedUpButton.setBounds(450, 450, 150, 50);

        speedUpButton.setBackground(Color.ORANGE);

        speedUpButton.setForeground(Color.BLACK);

        speedUpButton.setFocusPainted(false);

        speedUpButton.setBorderPainted(false);

        speedUpButton.addActionListener(e -> selectAbility(SPEED_UP));



        // Agregar efecto de hover para los botones

        healButton.addMouseListener(new HoverEffect(healButton));

        speedUpButton.addMouseListener(new HoverEffect(speedUpButton));

        rayButton.addMouseListener(new HoverEffect(rayButton));

        tripleShotButton.addMouseListener(new HoverEffect(tripleShotButton));



        add(rayButton);

        add(tripleShotButton);

        add(healButton);

        add(speedUpButton);



        hideAbilityButtons();

    }



    private void selectAbility(int ability) {

        // Si la habilidad no ha sido seleccionada, añádela

        if (!selectedAbilities.contains(ability)) {

            selectedAbilities.add(ability);

        }

        

        if (ability == HEAL) {

            lives = Math.min(lives + 1, 3);

        } else if (ability == SPEED_UP) {

            playerSpeed += 10; // Aumenta la velocidad del jugador

        }



        choosingAbility = false;

        levelCompleted = false;



        hideAbilityButtons();

        startNewLevel();

    }



    @Override

    protected void paintComponent(Graphics g) {

        super.paintComponent(g);


        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        // Dibuja el jugador

        g.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, this);


        if (playerHit && (System.currentTimeMillis() - playerHitTime < 200)) {
            g.setColor(Color.RED);
            g.fillRect(playerX, playerY, playerWidth, playerHeight);
        } else {
            g.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, this);
        }

        // Dibuja los invasores

        for (Rectangle invader : invaders) {

            g.drawImage(enemyImage, invader.x, invader.y, invader.width, invader.height, this);

        }



        // Dibuja los proyectiles del jugador

        g.setColor(Color.YELLOW);

        for (Rectangle bullet : bullets) {

            g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);

        }



        // Dibuja los proyectiles de los enemigos

        g.setColor(Color.ORANGE);

        for (Rectangle enemyBullet : enemyBullets) {

            g.fillRect(enemyBullet.x, enemyBullet.y, enemyBullet.width, enemyBullet.height);

        }



        // Dibuja partículas

        for (Particle particle : particles) {

            particle.draw(g);

        }



        // Dibuja las vidas restantes y el puntaje

        g.setColor(Color.WHITE);

        g.drawString("Lives: " + lives, 10, 20);

        g.drawString("Score: " + score, 10, 40);



        // Mensaje de victoria

        if (levelCompleted) {

            drawMessage(g, "Level Completed! Select your ability", Color.GREEN, getHeight() / 2 - 100);

        }



        // Mensaje de derrota

        if (gameOver) {

            drawMessage(g, "Game Over! Press R to Restart", Color.RED, getHeight() / 2);

        }

    }



    private void drawMessage(Graphics g, String message, Color color, int yPosition) {

        g.setColor(color);

        g.setFont(new Font("Courier", Font.BOLD, 30));

        FontMetrics metrics = g.getFontMetrics();

        int x = (getWidth() - metrics.stringWidth(message)) / 2;

        g.drawString(message, x, yPosition);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver || choosingAbility) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        // Actualiza las partículas
        updateParticles(); // Asegúrate de que las partículas se actualicen en cada ciclo

        // Mueve los proyectiles del jugador
        for (int i = 0; i < bullets.size(); i++) {
            Rectangle bullet = bullets.get(i);
            bullet.y -= 5;
            if (bullet.y < 0) {
                bullets.remove(i--);
            }
        }

        // Verificar si todos los invasores han sido eliminados
        if (invaders.isEmpty()) {
            levelCompleted = true;
            choosingAbility = true;
            showAbilityButtons();
            return;
        }

        // Mueve los proyectiles de los enemigos solo si pueden disparar
        if (enemiesCanShoot) {
            for (int i = 0; i < enemyBullets.size(); i++) {
                Rectangle enemyBullet = enemyBullets.get(i);
                enemyBullet.y += 5;
                if (enemyBullet.y > getHeight()) {
                    enemyBullets.remove(i--);
                }
            }
        }

        // Movimiento automático de invasores
        boolean changeDirection = false;
        for (Rectangle invader : invaders) {
            invader.x += invaderDirection;
            if (invader.x < 0 || invader.x > getWidth() - invader.width) {
                changeDirection = true;
            }
        }

        if (changeDirection) {
            invaderDirection *= -1;
            for (Rectangle invader : invaders) {
                invader.y += 10;
                if (invader.y > getHeight() - invader.height) {
                    gameOver = true;
                    return;
                }
            }
        }

        // Revisa colisiones entre proyectiles del jugador e invasores
        for (int i = 0; i < bullets.size(); i++) {
            Rectangle bullet = bullets.get(i);
            boolean bulletRemoved = false;

            for (int j = 0; j < invaders.size(); j++) {
                Rectangle invader = invaders.get(j);
                if (bullet.intersects(invader)) {
                    enemyHealth--; // Resta vida al enemigo

                    // Crea efecto de impacto rojo sobre el enemigo
                    createImpactEffect(invader.x, invader.y, Color.RED);

                    if (selectedAbilities.contains(RAY)) {
                        // Si el rayo está seleccionado, atraviesa enemigos
                        score += 100; // Añadir puntos al puntaje
                    } else {
                        bullets.remove(i--);
                        bulletRemoved = true;
                    }

                    if (enemyHealth <= 0) { // Si el enemigo se queda sin vida, se elimina
                        invaders.remove(j);
                        score += 100;
                        enemyHealth = 2; // Resetea la vida del siguiente enemigo
                    }

                    if (bulletRemoved) break;
                }
            }
        }

        // Revisa colisiones entre proyectiles enemigos y el jugador
        for (int i = 0; i < enemyBullets.size(); i++) {
            Rectangle enemyBullet = enemyBullets.get(i);
            Rectangle playerHitbox = new Rectangle(playerX, playerY, playerImage.getWidth(null), playerImage.getHeight(null)); // Ajuste de la hitbox del jugador

            if (enemyBullet.intersects(playerHitbox)) {
                enemyBullets.remove(i--);
                lives--;

                // Crea efecto de impacto rojo sobre el jugador
                createImpactEffect(playerX, playerY, Color.RED);

                if (lives <= 0) {
                    gameOver = true;
                }
            }
        }

     // Dentro del método actionPerformed, ajusta las llamadas a createParticle para pasar un color:
        if (moveLeft && playerX > 0) {
            playerX -= playerSpeed;
            createParticle(playerX + playerWidth, playerY + playerHeight / 2, Color.WHITE); // Crear partículas blancas al moverse a la izquierda
        }
        if (moveRight && playerX < getWidth() - playerWidth) {
            playerX += playerSpeed;
            createParticle(playerX, playerY + playerHeight / 2, Color.WHITE); // Crear partículas blancas al moverse a la derecha
        }

        // Disparar
        if (canShoot && currentTime - lastShotTime > SHOOT_COOLDOWN) {
            lastShotTime = currentTime;
            bullets.add(new Rectangle(playerX + playerWidth / 2 - 2, playerY, 4, 10));
            createParticle(playerX + playerWidth / 2, playerY, Color.YELLOW); // Crear partículas amarillas al disparar

            // Disparo múltiple si está seleccionado el Triple Shot
            if (selectedAbilities.contains(TRIPLE_SHOT)) {
                bullets.add(new Rectangle(playerX + playerWidth / 2 - 2 - 20, playerY, 4, 10));
                bullets.add(new Rectangle(playerX + playerWidth / 2 - 2 + 20, playerY, 4, 10));
            }
        }
        repaint();
    }




    private void updateParticles() {
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            if (particle.isExpired()) {
                particles.remove(i--);
            } else {
                particle.update();
            }
        }
    }



    private void createParticle(int x, int y, Color color) {
        particles.add(new Particle(x, y, random, color));
    }


    private void startNewLevel() {

        random = new Random();

        invaders = new ArrayList<>();

        bullets = new ArrayList<>();

        enemyBullets = new ArrayList<>();

        particles = new ArrayList<>();

        moveLeft = false;

        moveRight = false;

        canShoot = false;

        gameOver = false;

        levelCompleted = false;

        choosingAbility = false;
        
        level ++;
 
        // Colocar invasores

        for (int i = 0; i < 5; i++) {

            for (int j = 0; j < 3; j++) {

                invaders.add(new Rectangle(150 + i * 80, 30 + j * 60, 64, 64));

            }

        }

        timer = new Timer(20, this);

        timer.start();

    }



    private void showAbilityButtons() {

        rayButton.setVisible(true);

        tripleShotButton.setVisible(true);

        healButton.setVisible(true);

        speedUpButton.setVisible(true);

    }



    private void hideAbilityButtons() {

        rayButton.setVisible(false);

        tripleShotButton.setVisible(false);

        healButton.setVisible(false);

        speedUpButton.setVisible(false);

    }



    @Override

    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {

            moveLeft = true;

        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {

            moveRight = true;

        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {

            canShoot = true;

        }

        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {

            startNewLevel();

        }

    }



    @Override

    public void keyReleased(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {

            moveLeft = false;

        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {

            moveRight = false;

        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {

            canShoot = false;

        }

    }



    @Override

    public void keyTyped(KeyEvent e) {

    }



    // Clase para manejar las partículas

    private class Particle {
        private int x, y, life;
        private final int MAX_LIFE = 20;
        private final Random random;
        private final int size = 4;
        private final Color color;

        public Particle(int x, int y, Random random, Color color) {
            this.x = x;
            this.y = y;
            this.random = random;
            this.life = MAX_LIFE;
            this.color = color;
        }

        public void update() {
            x += random.nextInt(5) - 2;
            y += random.nextInt(5) - 2;
            life--;
        }

        public boolean isExpired() {
            return life <= 0;
        }

        public void draw(Graphics g) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * ((float) life / MAX_LIFE))));
            g.fillRect(x, y, size, size);
        }
    }




     


    // Clase para manejar el efecto de hover en los botones

    private class HoverEffect extends MouseAdapter {

        private final JButton button;



        public HoverEffect(JButton button) {

            this.button = button;

        }



        @Override

        public void mouseEntered(MouseEvent e) {

            button.setBackground(button.getBackground().darker());

        }



        @Override

        public void mouseExited(MouseEvent e) {

            button.setBackground(button.getBackground().brighter());

        }

    }

    private void createImpactEffect(int x, int y, Color color) {
        for (int i = 0; i < 10; i++) { // Crea varias partículas para el efecto
            particles.add(new Particle(x + random.nextInt(10) - 5, y + random.nextInt(10) - 5, random, color));
        }
    }



public static void main(String[] args) {

    JFrame frame = new JFrame("Space Invaders");

    SpaceInvaders game = new SpaceInvaders();

    frame.add(game);

    frame.pack();

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame.setVisible(true);

}

}
