
package locro;







import javax.swing.*;



import java.awt.*;



import java.awt.event.*;



import java.util.ArrayList;



import java.util.Random;



import javax.swing.Timer;

import java.awt.Rectangle;

import java.awt.Image;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.util.ArrayList;



public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {





	private Timer endGameTimer; // Temporizador para esperar antes de terminar el juego



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



 private int level =4;



 private int enemyHealth = 2; // Vida base de los enemigos (2 golpes)



 private boolean enemiesCanShoot = false ; // Indica si los enemigos pueden disparar



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



 private int bossHealth; // Vida del jefe

 private boolean bossExists = false; // Indica si el jefe está presente

 private Boss boss;



 public SpaceInvaders() {



 // Cargar imágenes



 playerImage = new ImageIcon(getClass().getResource("/imagen/player.png")).getImage();



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



 // Dibuja el fondo

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

 if (bossExists) {

 boss.draw(g, this);

 }



 // Dibuja las vidas restantes y el puntaje

 g.setColor(Color.WHITE);

 g.drawString("Lives: " + lives, 10, 20);

 g.drawString("Score: " + score, 10, 40);



 

 // Mensaje de derrota

 if (gameOver && !invaders.isEmpty()) {

 drawMessage(g, "Apreta R para reiniciar", Color.RED, getHeight() / 2);

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

 updateParticles();



 // Mueve los proyectiles del jugador

 for (int i = 0; i < bullets.size(); i++) {

 Rectangle bullet = bullets.get(i);

 bullet.y -= 5;

 if (bullet.y < 0) {

 bullets.remove(i--);

 }

 }



 if (invaders.isEmpty() && !bossExists) {

 if (level == 4) { 

 } else {

 level ++;

 levelCompleted = true;

 choosingAbility = true;

 showAbilityButtons();

 }

 return;

 }



 // Movimiento y disparo de enemigos en nivel 2 o superior

 if (enemiesCanShoot) {

 for (int i = 0; i < enemyBullets.size(); i++) {

 Rectangle enemyBullet = enemyBullets.get(i);

 enemyBullet.y += 5;

 if (enemyBullet.y > getHeight()) {

 enemyBullets.remove(i--);

 }

 }



 if (random.nextInt(100) < 0.01) { // 10% de probabilidad de disparo en cada ciclo

 int randomInvaderIndex = random.nextInt(invaders.size());

 Rectangle randomInvader = invaders.get(randomInvaderIndex);

 enemyBullets.add(new Rectangle(randomInvader.x + randomInvader.width / 2 - 2, randomInvader.y + randomInvader.height, 4, 10));

 }

 }



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



 // Revisa colisiones entre proyectiles del jugador e invasores o jefe

 for (int i = 0; i < bullets.size(); i++) {

 Rectangle bullet = bullets.get(i);

 boolean bulletRemoved = false;



 for (int j = 0; j < invaders.size(); j++) {

 Rectangle invader = invaders.get(j);

 if (bullet.intersects(invader)) {

 // Si es el jefe

 {

 enemyHealth--;

 createImpactEffect(invader.x, invader.y, Color.RED);

 if (enemyHealth <= 0) {

 invaders.remove(j);

 score += 100;

 enemyHealth = 2;

 }

 }



 if (!selectedAbilities.contains(RAY)) {

 bullets.remove(i--);

 bulletRemoved = true;

 }



 if (bulletRemoved) break;

 }

 }

 }

 

 if (bossExists) {

 boss.move(getWidth(), playerX, playerY);

 

 // Verifica si el jefe es golpeado por un proyectil

 for (int i = 0; i < bullets.size(); i++) {

 Rectangle bullet = bullets.get(i);

 if (boss.isHit(bullet)) {

 boss.takeDamage();

 createImpactEffect(boss.getX(), boss.getY(), Color.RED);

 bullets.remove(i--);

 if (boss.isDead()) {

 boss.launchTowardsPlayer(playerX, playerY);

 }

 break;

 }

 }



 // Verifica colisiones entre las balas del jefe y el jugador

 Rectangle playerHitbox = new Rectangle(playerX, playerY, playerWidth, playerHeight);

 if (boss.checkCollisionWithPlayer(playerHitbox)) {

 lives = 0; // El jugador pierde todas las vidas si es golpeado

 gameOver = true;

 }

 }

 



 // Verifica colisiones entre proyectiles enemigos y el jugador

 for (int i = 0; i < enemyBullets.size(); i++) {

 Rectangle enemyBullet = enemyBullets.get(i);

 Rectangle playerHitbox = new Rectangle(playerX, playerY, playerImage.getWidth(null), playerImage.getHeight(null));



 if (enemyBullet.intersects(playerHitbox)) {

 enemyBullets.remove(i--);

 lives--;



 createImpactEffect(playerX, playerY, Color.RED);



 if (lives <= 0) {

 gameOver = true;

 }

 }

 }



 

 if (moveLeft && playerX > 0) {

 playerX -= playerSpeed;

 createParticle(playerX + playerWidth, playerY + playerHeight / 2, Color.WHITE);

 }

 if (moveRight && playerX < getWidth() - playerWidth) {

 playerX += playerSpeed;

 createParticle(playerX, playerY + playerHeight / 2, Color.WHITE);

 }



 if (canShoot && currentTime - lastShotTime > SHOOT_COOLDOWN) {

 lastShotTime = currentTime;

 bullets.add(new Rectangle(playerX + playerWidth / 2 - 2, playerY, 4, 10));

 createParticle(playerX + playerWidth / 2, playerY, Color.YELLOW);



 if (selectedAbilities.contains(TRIPLE_SHOT)) {

 bullets.add(new Rectangle(playerX + playerWidth / 2 - 2 - 20, playerY, 4, 10));

 bullets.add(new Rectangle(playerX + playerWidth / 2 - 2 + 20, playerY, 4, 10));

 }

 }



 repaint();

 }





 private void showVictoryMessage() {

 gameOver = true;

 bossExists = false; 

 invaders.clear(); 



 if (timer != null) {

 timer.stop();

 }



 Timer restartTimer = new Timer(3000, ev -> {

 drawMessage(getGraphics(), "¡Victoria! Has derrotado al jefe.", Color.GREEN, getHeight() / 2);

 resetGame();

 });

 restartTimer.setRepeats(false);

 restartTimer.start();



 repaint(); 

 }





 private void resetGame() {

 level = 0; // Reinicia el nivel

 lives = 3; // Reinicia las vidas

 score = 0; // Reinicia el puntaje
 playerSpeed= 10;
 selectedAbilities.clear(); // Limpia las habilidades seleccionadas

 gameOver = false; // Reinicia el estado de Game Over

 startNewLevel(); // Comienza un nuevo juego desde el nivel 1

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



 class Boss {

	    private int x, y, width, height;
	    private int health;
	    private int speed;
	    private int direction;
	    private Image image;
	    private Image imageHalfHealth; // Nueva imagen para cuando el jefe está en mitad de vida
	    private ArrayList<Rectangle> bossBullets; // Lista de balas del jefe
	    private int bulletSize; // Tamaño de las balas del jefe
	    private boolean launching = false; // Indica si el jefe se está lanzando hacia el jugador
	    private int launchSpeed = 4; // Velocidad del lanzamiento
	    private int directionX; // Dirección en el eje X durante el lanzamiento
	    private int directionY; // Dirección en el eje Y durante el lanzamiento
	    private boolean hasLaunched = false; // Indica si el jefe ya se ha lanzado

	    public Boss(int x, int y, int health) {
	        this.x = x;
	        this.y = y;
	        this.width = 256;
	        this.height = 256;
	        this.health = health = 100;
	        this.speed = 3 / 2; // Velocidad inicial
	        this.direction = 1; // 1: derecha, -1: izquierda
	        this.image = new ImageIcon(getClass().getResource("/imagen/jefe.png")).getImage();
	        this.imageHalfHealth = new ImageIcon(getClass().getResource("/imagen/jefedios.png")).getImage(); // Cargar nueva imagen
	        this.bossBullets = new ArrayList<>();
	        this.bulletSize = 10; // Tamaño inicial de las balas

	        endGameTimer = new Timer(6000, new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                endGame();
	                endGameTimer.stop(); // Detiene el temporizador después de la ejecución
	            }
	        });
	    }

	    public void launchTowardsPlayer(int playerX, int playerY) {
	        launching = true;
	        // Calcula la dirección hacia el jugador
	        int deltaX = playerX - x;
	        int deltaY = playerY - y;
	        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	        directionX = (int) (deltaX / distance * launchSpeed);
	        directionY = (int) (deltaY / distance * launchSpeed);
	    }

	    public void move(int panelWidth, int playerX, int playerY) {
	        if (launching) {
	            x += directionX;
	            y += directionY;

	            if (y + height >= 700) {
	                health = 0;
	                launching = false;
	                level = 0;
	                resetGame();
	            }

	            // Comprueba si el jefe ha alcanzado al jugador o ha salido de la pantalla
	            if (x < 0 || x > panelWidth || y < 0 || y > panelWidth) {
	                launching = false;
	            }
	        } else if (!hasLaunched) {
	            x += speed * direction;
	            if (x < 0 || x + width > panelWidth) {
	                direction *= -1;
	            }

	            // Ajusta el tamaño de las balas según la vida del jefe
	            if (health <= 50) {
	                bulletSize = 20; // Tamaño más grande cuando la vida es 50 o menos
	            } else {
	                bulletSize = 10; // Tamaño normal
	            }

	            // Cada cierto tiempo, disparar
	            if (Math.random() < 0.02) { // 5% de probabilidad de disparo en cada ciclo
	                shoot();
	            }

	            // Mover las balas del jefe
	            for (int i = 0; i < bossBullets.size(); i++) {
	                Rectangle bullet = bossBullets.get(i);
	                bullet.y += 4; // Velocidad de la bala del jefe
	                if (bullet.y > panelWidth) {
	                    bossBullets.remove(i--); // Elimina la bala si sale de la pantalla
	                }
	            }
	        }
	    }

	    private void shoot() {
	        // Implementación del disparo del jefe
	        bossBullets.add(new Rectangle(x + width / 2 - bulletSize / 2, y + height, bulletSize, bulletSize));
	    }

	    public void draw(Graphics g, JPanel panel) {
	        if (health <= 50) { // Cambia la imagen cuando la vida del jefe esté por debajo del 50%
	            g.drawImage(imageHalfHealth, x, y, width, height, panel);
	        } else {
	            g.drawImage(image, x, y, width, height, panel);
	        }

	        g.setColor(Color.RED);
	        for (Rectangle bullet : bossBullets) {
	            g.fillRect(bullet.x, bullet.y, bulletSize, bulletSize); // Dibuja las balas del jefe con el tamaño actual
	        }
	    }

	    public boolean isHit(Rectangle bullet) {
	        Rectangle hitbox = new Rectangle(x, y, width, height);
	        return hitbox.intersects(bullet);
	    }

	    public void takeDamage() {
	        health--;
	    }

	    public boolean isDead() {
	        return health <= 0;
	    }

	    public int getX() {
	        return x;
	    }

	    public int getY() {
	        return y;
	    }

	    public boolean isLaunching() {
	        return launching;
	    }

	    public int getHealth() {
	        return health;
	    }

	    public void reduceHealth(int amount) {
	        health -= amount;
	        if (health <= 0 && !launching) {
	            endGameTimer.start(); // Inicia el temporizador
	            bossExists = false;
	            resetGame();
	        }
	    }

	    private void endGame() {
	        System.out.println("¡El jefe ha sido derrotado! Fin del juego.");
	        showVictoryMessage();
	        resetGame();
	    }

	    public boolean checkCollisionWithPlayer(Rectangle playerHitbox) {
	        // Colisión mientras se lanza
	        if (launching) {
	            Rectangle hitbox = new Rectangle(x, y, width, height);
	            if (hitbox.intersects(playerHitbox)) {
	                return true;
	            }
	        }
	        // Colisión con balas del jefe
	        for (Rectangle bullet : bossBullets) {
	            if (bullet.intersects(playerHitbox)) {
	                bossBullets.remove(bullet);
	                return true;
	            }
	        }
	        return false;
	    }
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

 

 if (level >= 2 && level < 4) {

 enemiesCanShoot = true;

 } else {

 enemiesCanShoot = false;

 }

 // Colocar invasores normales en niveles 1 y 2

 if (level < 4) {

 for (int i = 0; i < 5; i++) {

 for (int j = 0; j < 3; j++) {

 invaders.add(new Rectangle(150 + i * 80, 30 + j * 60, 64, 64));

 }

 }

 }



 if (level == 4) {

 boss = new Boss(300, 30, bossHealth); // Inicializa el jefe con la vida especificada

 bossExists = true;

 invaders.clear(); // Asegúrate de que no haya otros invasores

 } else {

 bossExists = false;

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

 resetGame(); // Llama a resetGame para reiniciar el juego correctamente

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