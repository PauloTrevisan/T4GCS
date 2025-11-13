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

/**
 * ##############################################################
 * ###               S P A C E     E S C A P E                ###
 * ##############################################################
 * ###                  Versão Alpha 0.3                      ###
 * ##############################################################
 * ### Objetivo: desviar dos meteoros que caem.               ###
 * ### Cada colisão tira uma vida. Sobreviva o máximo que     ###
 * ### conseguir!                                             ###
 * ##############################################################
 * ### Conversão para Java - Baseado no código original       ###
 * ### Prof. Filipo Novo Mor - github.com/ProfessorFilipo     ###
 * ##############################################################
 */
public class SpaceEscape extends JPanel implements ActionListener, KeyListener {

    // Configurações gerais
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int FPS = 60;

    // Assets
    private static final String ASSET_BACKGROUND = "fundo_espacial.png";
    private static final String ASSET_PLAYER = "nave001.png";
    private static final String ASSET_METEOR = "meteoro001.png";
    private static final String ASSET_SOUND_POINT = "classic-game-action-positive-5-224402.wav";
    private static final String ASSET_SOUND_HIT = "stab-f-01-brvhrtz-224599.wav";
    private static final String ASSET_MUSIC = "distorted-future-363866.wav";

    // Cores para fallback
    private static final Color WHITE = Color.WHITE;
    private static final Color RED = new Color(255, 60, 60);
    private static final Color BLUE = new Color(60, 100, 255);

    // Imagens
    private BufferedImage background;
    private BufferedImage playerImg;
    private BufferedImage meteorImg;

    // Sons
    private Clip soundPoint;
    private Clip soundHit;
    private Clip music;

    // Variáveis de jogo
    private Rectangle playerRect;
    private int playerSpeed = 7;
    private ArrayList<Rectangle> meteorList;
    private ArrayList<Integer> meteorSpeeds; // lista para variar a velocidade dos meteoros
    private int meteorSpeed = 5;
    private int score = 0;
    private int lives = 3;
    private boolean running = true;
    private boolean gameOver = false;

    // Controles
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    // Timer
    private Timer timer;
    private Random random;

    public SpaceEscape() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        random = new Random();

        // Carrega recursos
        loadAssets();

        // Inicializa jogador
        playerRect = new Rectangle(WIDTH / 2 - 40, HEIGHT - 60, 80, 60);

        // Inicializa meteoros
        meteorList = new ArrayList<>();
        meteorSpeeds = new ArrayList<>(); // inicializa as velocidades
        for (int i = 0; i < 5; i++) {
            int x = random.nextInt(WIDTH - 40);
            int y = random.nextInt(500) - 500;
            meteorList.add(new Rectangle(x, y, 40, 40));
            meteorSpeeds.add(random.nextInt(10) + meteorSpeed); // randomiza as velocidades
        }

        // Inicia música de fundo
        playMusic();

        // Timer do jogo
        timer = new Timer(1000 / FPS, this);
        timer.start();
    }

    private void loadAssets() {
        // Carrega imagens
        background = loadImage(ASSET_BACKGROUND, WHITE, WIDTH, HEIGHT);
        playerImg = loadImage(ASSET_PLAYER, BLUE, 80, 60);
        meteorImg = loadImage(ASSET_METEOR, RED, 40, 40);

        // Carrega sons
        soundPoint = loadSound(ASSET_SOUND_POINT);
        soundHit = loadSound(ASSET_SOUND_HIT);
        music = loadSound(ASSET_MUSIC);
    }

    private BufferedImage loadImage(String filename, Color fallbackColor, int width, int height) {
        File file = new File(filename);
        if (file.exists()) {
            try {
                BufferedImage img = ImageIO.read(file);
                Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = result.createGraphics();
                g2d.drawImage(scaled, 0, 0, null);
                g2d.dispose();
                return result;
            } catch (IOException e) {
                System.out.println("Erro ao carregar imagem: " + filename);
            }
        }

        // Fallback: cria imagem colorida
        BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fallback.createGraphics();
        g2d.setColor(fallbackColor);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return fallback;
    }

    private Clip loadSound(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            try {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
                AudioFormat baseFormat = audioIn.getFormat();

                // Converte para formato compatível se necessário
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );

                AudioInputStream decodedAudioIn = AudioSystem.getAudioInputStream(decodedFormat, audioIn);
                Clip clip = AudioSystem.getClip();
                clip.open(decodedAudioIn);
                System.out.println("Som carregado: " + filename);
                return clip;
            } catch (Exception e) {
                System.out.println("Erro ao carregar som: " + filename);
                e.printStackTrace();
            }
        } else {
            System.out.println("Arquivo de som não encontrado: " + filename);
        }
        return null;
    }

    private void playMusic() {
        if (music != null) {
            try {
                FloatControl volume = (FloatControl) music.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(-20.0f); // Reduz volume (0.3 aproximadamente)
            } catch (Exception e) {
                System.out.println("Não foi possível controlar o volume da música");
            }
            music.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("Música de fundo iniciada");
        } else {
            System.out.println("Música de fundo não disponível");
        }
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!running) return;

        // Movimento do jogador
        if (leftPressed) {
            playerRect.x -= playerSpeed;
        }
        if (rightPressed) {
            playerRect.x += playerSpeed;
        }

        // ============================================
        // WRAPAROUND - Teleporte nas bordas
        // ============================================
        // Se sair pela direita, aparece na esquerda
        if (playerRect.x >= WIDTH) {
            playerRect.x = -playerRect.width + 1;
        }
        // Se sair pela esquerda, aparece na direita
        else if (playerRect.x + playerRect.width <= 0) {
            playerRect.x = WIDTH - 1;
        }
        // ============================================

        // Movimento dos meteoros
        for (int i = 0; i < meteorList.size(); i++) { // for alterado para as novas velocidades
            Rectangle meteor = meteorList.get(i);
            int speed = meteorSpeeds.get(i);
            meteor.y += speed;

            // Saiu da tela
            if (meteor.y > HEIGHT) {
                meteor.y = random.nextInt(60) - 100;
                meteor.x = random.nextInt(WIDTH - meteor.width);
                meteorSpeeds.set(i, random.nextInt(10) + meteorSpeed); // nova linha para setar velocidades
                score++;
                playSound(soundPoint);
            }

            // Colisão
            if (meteor.intersects(playerRect)) {
                lives--;
                meteor.y = random.nextInt(60) - 100;
                meteor.x = random.nextInt(WIDTH - meteor.width);
                playSound(soundHit);

                if (lives <= 0) {
                    running = false;
                    gameOver = true;
                    if (music != null) music.stop();
                }
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (!gameOver) {
            // Desenha fundo
            g2d.drawImage(background, 0, 0, null);

            // Desenha jogador (com wraparound visual)
            g2d.drawImage(playerImg, playerRect.x, playerRect.y, null);

            // Se a nave está saindo pela direita, desenha a parte que aparece na esquerda
            if (playerRect.x + playerRect.width > WIDTH) {
                g2d.drawImage(playerImg, playerRect.x - WIDTH, playerRect.y, null);
            }
            // Se a nave está saindo pela esquerda, desenha a parte que aparece na direita
            else if (playerRect.x < 0) {
                g2d.drawImage(playerImg, playerRect.x + WIDTH, playerRect.y, null);
            }

            // Desenha meteoros
            for (Rectangle meteor : meteorList) {
                g2d.drawImage(meteorImg, meteor.x, meteor.y, null);
            }

            // Desenha pontuação e vidas
            g2d.setColor(WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("Pontos: " + score + "   Vidas: " + lives, 10, 30);
        } else {
            // Tela de fim de jogo
            g2d.setColor(new Color(20, 20, 20));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            g2d.setColor(WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String endText = "Fim de jogo! Pressione qualquer tecla para sair.";
            String scoreText = "Pontuação final: " + score;

            FontMetrics fm = g2d.getFontMetrics();
            int x1 = (WIDTH - fm.stringWidth(endText)) / 2;
            int x2 = (WIDTH - fm.stringWidth(scoreText)) / 2;

            g2d.drawString(endText, x1, HEIGHT / 2 - 20);
            g2d.drawString(scoreText, x2, HEIGHT / 2 + 20);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            System.exit(0);
        }

        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

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