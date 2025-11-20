import javax.swing.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class SpaceEscape extends JPanel implements ActionListener, KeyListener {

    // Configurações básicas
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int FPS = 60;

    // Assets
    private static final String ASSET_BACKGROUND = "assets/fundo_espacial.png";
    private static final String ASSET_PLAYER = "assets/nave001.png";
    private static final String ASSET_METEOR = "assets/meteoro001.png";
    private static final String ASSET_METEOR_LIFE = "assets/meteoro_vida.png";
    private static final String ASSET_METEOR_DANGER = "assets/meteoro_perigo.png";
    private static final String ASSET_METEOR_MEGA = "assets/mega_meteoro.png";

    // Sons
    private static final String ASSET_SOUND_POINT = "assets/classic-game-action-positive-5-224402.wav";
    private static final String ASSET_SOUND_HIT = "assets/stab-f-01-brvhrtz-224599.wav";
    private static final String ASSET_SOUND_LIFE = "assets/meteoro_vida_audio.wav";
    private static final String ASSET_SOUND_DANGER_HIT = "assets/meteoro_perigo_audio.wav";

    // Músicas por fase (Feature 10)
    private static final String MUSIC_PHASE_1 = "assets/fase1.wav";
    private static final String MUSIC_PHASE_2 = "assets/fase2.wav";
    private static final String MUSIC_PHASE_3 = "assets/fase3.wav";

    // Fallback colors
    private static final Color WHITE = Color.WHITE;
    private static final Color RED = new Color(255, 60, 60);
    private static final Color BLUE = new Color(60, 100, 255);

    // Imagens
    private BufferedImage background;
    private BufferedImage playerImg;
    private BufferedImage meteorImg;
    private BufferedImage meteorLifeImg;
    private BufferedImage meteorDangerImg;
    private BufferedImage megaMeteorImg;

    // Sons
    private Clip soundPoint;
    private Clip soundHit;
    private Clip soundLife;
    private Clip soundDangerHit;
    private Clip music;

    // Jogador
    private Rectangle playerRect;
    private int playerSpeed = 7;

    // Meteoros
    private ArrayList<Rectangle> meteorList;
    private ArrayList<Integer> meteorSpeeds;
    private ArrayList<Boolean> meteorIsLife;
    private ArrayList<Boolean> meteorIsDanger;
    private int meteorSpeed = 5;
    private int meteorDangerSpeed = 5;

    // Mega meteoro (Feature 7)
    private Rectangle megaMeteorRect = null;
    private boolean megaMeteorActive = false;
    private int megaMeteorSpeed = 3;

    // Estado do jogo
    private int score = 0;
    private int lives = 3;
    private boolean running = true;
    private boolean gameOver = false;
    private boolean victory = false;
    private boolean introScreen = true; // Feature 1
    private static final int SCORE_TO_WIN = 100;

    // Shake de tela (Feature 11)
    private int shakeDuration = 0;
    private int shakeX = 0;
    private int shakeY = 0;

    // Controles
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    private Timer timer;
    private Random random;

    // Fases da música (Feature 10)
    private int currentPhase = 1;

    public SpaceEscape() {

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        random = new Random();
        loadAssets();

        // Jogador
        playerRect = new Rectangle(WIDTH / 2 - 40, HEIGHT - 60, 80, 60);

        // Meteoros
        meteorList = new ArrayList<>();
        meteorSpeeds = new ArrayList<>();
        meteorIsLife = new ArrayList<>();
        meteorIsDanger = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            int x = random.nextInt(WIDTH - 40);
            int y = random.nextInt(500) - 500;
            meteorList.add(new Rectangle(x, y, 40, 40));
            meteorSpeeds.add(random.nextInt(7) + meteorSpeed); // Feature 5
            meteorIsLife.add(false);
            meteorIsDanger.add(false);
        }

        playMusic(MUSIC_PHASE_1); // Feature 10
        timer = new Timer(1000 / FPS, this);
    }

    private void playSound(Clip clip) {
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }
    }

    // --------------------------
    // Carregamento de assets
    // --------------------------
    private void loadAssets() {
        background = loadImage(ASSET_BACKGROUND, WHITE, WIDTH, HEIGHT);
        playerImg = loadImage(ASSET_PLAYER, BLUE, 80, 60);
        meteorImg = loadImage(ASSET_METEOR, RED, 40, 40);
        meteorLifeImg = loadImage(ASSET_METEOR_LIFE, Color.GREEN, 40, 40); // Feature 8
        meteorDangerImg = loadImage(ASSET_METEOR_DANGER, Color.DARK_GRAY, 60, 60); // Feature 6
        megaMeteorImg = loadImage(ASSET_METEOR_MEGA, new Color(150, 0, 150), 120, 120); // Feature 7

        soundPoint = loadSound(ASSET_SOUND_POINT);
        soundHit = loadSound(ASSET_SOUND_HIT);
        soundLife = loadSound(ASSET_SOUND_LIFE); // Feature 9
        soundDangerHit = loadSound(ASSET_SOUND_DANGER_HIT); // Feature 9
    }

    private BufferedImage loadImage(String filename, Color fallbackColor, int width, int height) {
        File file = new File(filename);
        if (file.exists()) {
            try {
                BufferedImage img = ImageIO.read(file);
                Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = result.createGraphics();
                g.drawImage(scaled, 0, 0, null);
                g.dispose();
                return result;
            } catch (IOException e) {
                System.out.println("Erro carregando imagem: " + filename);
            }
        }

        BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fallback.createGraphics();
        g.setColor(fallbackColor);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return fallback;
    }

    private Clip loadSound(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) return null;

            AudioInputStream audio = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            return clip;

        } catch (Exception e) {
            System.out.println("Erro carregando som: " + filename);
        }
        return null;
    }

    private void playMusic(String filename) {
        try {
            if (music != null) music.stop();
            File file = new File(filename);
            AudioInputStream audio = AudioSystem.getAudioInputStream(file);
            music = AudioSystem.getClip();
            music.open(audio);
            music.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) { }
    }

    // --------------------------
    // Lógica da fase (Feature 10)
    // --------------------------
    private void updatePhase() {
        if (score >= 30 && currentPhase == 1) {
            currentPhase = 2;
            playMusic(MUSIC_PHASE_2);
        }
        if (score >= 70 && currentPhase == 2) {
            currentPhase = 3;
            playMusic(MUSIC_PHASE_3);
        }
    }

    // --------------------------
    // Tela treme (Feature 11)
    // --------------------------
    private void startShake() {
        shakeDuration = 20;
    }

    private void updateShake() {
        if (shakeDuration > 0) {
            shakeX = random.nextInt(11) - 5;
            shakeY = random.nextInt(11) - 5;
            shakeDuration--;
        } else {
            shakeX = 0;
            shakeY = 0;
        }
    }

    // --------------------------
    // Mega Meteoro (Feature 7)
    // --------------------------
    private void updateMegaMeteor() {

        if (!megaMeteorActive && score >= 50) {
            megaMeteorActive = true;
            megaMeteorRect = new Rectangle(random.nextInt(WIDTH - 140), -200, 140, 140);
        }

        if (!megaMeteorActive) return;

        megaMeteorRect.y += megaMeteorSpeed;

        if (megaMeteorRect.y > HEIGHT) {
            megaMeteorActive = false;
            return;
        }

        if (megaMeteorRect.intersects(playerRect)) {
            lives -= 3;
            playSound(soundHit);
            startShake();

            megaMeteorActive = false;

            if (lives <= 0) {
                running = false;
                gameOver = true;
                if (music != null) music.stop();
            }
        }
    }

    // --------------------------
    // Lógica principal
    // --------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (introScreen || gameOver || victory || !running) return;

        updateShake();
        updatePhase();

        // Inclui movimento vertical da nave (Feature 3)
        if (leftPressed) playerRect.x -= playerSpeed;
        if (rightPressed) playerRect.x += playerSpeed;
        if (upPressed && playerRect.y > 0) playerRect.y -= playerSpeed;
        if (downPressed && playerRect.y + playerRect.height < HEIGHT) playerRect.y += playerSpeed;

        // Teletransporte nas bordas (Feature 4)
        if (playerRect.x >= WIDTH) playerRect.x = -playerRect.width + 1;
        else if (playerRect.x + playerRect.width <= 0) playerRect.x = WIDTH - 1;

        // Movimento dos meteoros
        for (int i = 0; i < meteorList.size(); i++) {

            Rectangle meteor = meteorList.get(i);
            int speed = meteorSpeeds.get(i);
            boolean isLife = meteorIsLife.get(i);
            boolean isDanger = meteorIsDanger.get(i);

            // Meteoro perigoso tira 2 vidas (Feature 6)
            if (isDanger) {
                meteor.y += meteorDangerSpeed;
                meteor.width = 60;
                meteor.height = 60;
            } else {
                meteor.y += speed;
            }

            // Saiu da tela
            if (meteor.y > HEIGHT) {

                meteor.y = random.nextInt(60) - 100;
                meteor.x = random.nextInt(WIDTH - meteor.width);
                meteorSpeeds.set(i, random.nextInt(7) + meteorSpeed);

                int chance = random.nextInt(100);

                // Meteoro perigoso (Feature 6)
                if (chance < 8) {
                    meteorIsDanger.set(i, true);
                    meteorIsLife.set(i, false);

                // Meteoro de vida extra (Feature 8)
                } else if (chance < 18) {
                    meteorIsDanger.set(i, false);
                    meteorIsLife.set(i, true);

                } else {
                    meteorIsDanger.set(i, false);
                    meteorIsLife.set(i, false);
                }

                if (!isLife) {
                    score++;
                    playSound(soundPoint);

                    if (score >= SCORE_TO_WIN) {
                        victory = true; // Feature 2
                        running = false;
                        if (music != null) music.stop();
                    }
                }
            }

            // Colisão com jogador
            if (meteor.intersects(playerRect)) {

                meteor.y = random.nextInt(60) - 100;
                meteor.x = random.nextInt(WIDTH - meteor.width);
                meteorSpeeds.set(i, random.nextInt(7) + meteorSpeed);

                boolean previousLife = isLife;
                boolean previousDanger = isDanger;

                meteorIsLife.set(i, false);
                meteorIsDanger.set(i, false);

                // Meteoro de vida extra (Feature 8)
                if (previousLife) {
                    lives++;
                    playSound(soundLife);

                // Meteoro perigoso tira 2 vidas (Feature 6)
                } else if (previousDanger) {
                    lives -= 2;
                    playSound(soundDangerHit);
                    startShake(); // Feature 11

                // Meteoro normal tira 1 vida
                } else {
                    lives--;
                    playSound(soundHit);
                    startShake(); // Feature 11
                }

                if (lives <= 0) {
                    running = false;
                    gameOver = true; // Feature 2
                    if (music != null) music.stop();
                }
            }
        }

        updateMegaMeteor(); // Feature 7
        repaint();
    }

    // --------------------------
    // Desenho de tela
    // --------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.translate(shakeX, shakeY);

        // Tela de introdução (Feature 1)
        if (introScreen) {

            g2d.setColor(new Color(20, 20, 20));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            g2d.setColor(WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));

            String title = "SPACE ESCAPE";
            FontMetrics fm = g2d.getFontMetrics();
            int x1 = (WIDTH - fm.stringWidth(title)) / 2;
            g2d.drawString(title, x1, HEIGHT / 2 - 50);

            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            String sub = "Pressione ENTER para iniciar";
            fm = g2d.getFontMetrics();
            int x2 = (WIDTH - fm.stringWidth(sub)) / 2;
            g2d.drawString(sub, x2, HEIGHT / 2 + 20);

            return;
        }

        // Tela de encerramento (vitória ou derrota) — Feature 2
        if (gameOver || victory) {

            g2d.setColor(new Color(20, 20, 20));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            g2d.setColor(WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));

            String text = victory ?
                "VOCÊ VENCEU! Pressione qualquer tecla para sair." :
                "Fim de jogo! Pressione qualquer tecla para sair.";

            String scoreText = "Pontuação final: " + score;

            FontMetrics fm = g2d.getFontMetrics();
            int x1 = (WIDTH - fm.stringWidth(text)) / 2;
            int x2 = (WIDTH - fm.stringWidth(scoreText)) / 2;

            g2d.drawString(text, x1, HEIGHT / 2 - 20);
            g2d.drawString(scoreText, x2, HEIGHT / 2 + 20);

            return;
        }

        // --- JOGO RODANDO ---

        g2d.drawImage(background, 0, 0, null);

        // Nave
        g2d.drawImage(playerImg, playerRect.x, playerRect.y, null);

        // Wraparound visual (Feature 4)
        if (playerRect.x + playerRect.width > WIDTH) {
            g2d.drawImage(playerImg, playerRect.x - WIDTH, playerRect.y, null);
        }
        if (playerRect.x < 0) {
            g2d.drawImage(playerImg, playerRect.x + WIDTH, playerRect.y, null);
        }

        // Meteoros
        for (int i = 0; i < meteorList.size(); i++) {
            Rectangle meteor = meteorList.get(i);

            if (meteorIsDanger.get(i)) {
                g2d.drawImage(meteorDangerImg, meteor.x, meteor.y, null);
            } else if (meteorIsLife.get(i)) {
                g2d.drawImage(meteorLifeImg, meteor.x, meteor.y, null);
            } else {
                g2d.drawImage(meteorImg, meteor.x, meteor.y, null);
            }
        }

        // Mega Meteoro — Feature 7
        if (megaMeteorActive && megaMeteorRect != null) {
            g2d.drawImage(megaMeteorImg, megaMeteorRect.x, megaMeteorRect.y, null);
        }

        // HUD
        g2d.setColor(WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("Pontos: " + score + "   Vidas: " + lives, 10, 30);
    }

    // --------------------------
    // Inputs
    // --------------------------
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver || victory) System.exit(0);

        if (introScreen) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                introScreen = false;
                timer.start();
            }
            return;
        }

        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_UP) upPressed = true;
        if (key == KeyEvent.VK_DOWN) downPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;
        if (key == KeyEvent.VK_UP) upPressed = false;
        if (key == KeyEvent.VK_DOWN) downPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // --------------------------
    // Main
    // --------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🚀 Space Escape");
            SpaceEscape game = new SpaceEscape();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}