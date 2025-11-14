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
    private static final String ASSET_METEOR_LIFE = "meteoro_vida.png";
    private static final String ASSET_METEOR_DANGER = "meteoro_perigo.png";
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
    private BufferedImage meteorLifeImg;
    private BufferedImage meteorDangerImg;

    // Sons
    private Clip soundPoint;
    private Clip soundHit;
    private Clip music;

    // Variáveis de jogo
    private Rectangle playerRect;
    private int playerSpeed = 7;
    private ArrayList<Rectangle> meteorList;
    private ArrayList<Integer> meteorSpeeds;
    private ArrayList<Boolean> meteorIsLife;
    private ArrayList<Boolean> meteorIsDanger;
    private int meteorSpeed = 5;
    private int meteorDangerSpeed = 5;
    private int meteorDangerWidth = 60;
    private int meteorDangerHeight = 60;
    private int score = 0;
    private int lives = 3;
    private boolean running = true;
    private boolean gameOver = false;
    private boolean introScreen = true;
    private boolean victory = false;
    private static final int SCORE_TO_WIN = 100;

    // Controles
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

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
        meteorSpeeds = new ArrayList<>();
        meteorIsLife = new ArrayList<>();
        meteorIsDanger = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            int x = random.nextInt(WIDTH - 40);
            int y = random.nextInt(500) - 500;
            meteorList.add(new Rectangle(x, y, 40, 40));
            meteorSpeeds.add(random.nextInt(7) + meteorSpeed); // randomiza as velocidades
            meteorIsLife.add(false);
            meteorIsDanger.add(false);
        }

        // Inicia música de fundo
        playMusic();

        // Timer do jogo
        timer = new Timer(1000 / FPS, this);
    }

    private void loadAssets() {
        // Carrega imagens
        background = loadImage(ASSET_BACKGROUND, WHITE, WIDTH, HEIGHT);
        playerImg = loadImage(ASSET_PLAYER, BLUE, 80, 60);
        meteorImg = loadImage(ASSET_METEOR, RED, 40, 40);
        meteorLifeImg = loadImage(ASSET_METEOR_LIFE, Color.GREEN, 40, 40);
        meteorDangerImg = loadImage(ASSET_METEOR_DANGER, Color.DARK_GRAY, meteorDangerWidth, meteorDangerHeight);

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
        if (introScreen || gameOver || !running) return;

        // Movimento do jogador (horizontal)
        if (leftPressed && playerRect.x > 0) {
            playerRect.x -= playerSpeed;
        }
        if (rightPressed && playerRect.x + playerRect.width < WIDTH) {
            playerRect.x += playerSpeed;
        }

        // Movimento do jogador (vertical)
        if (upPressed && playerRect.y > 0) {
            playerRect.y -= playerSpeed;
        }
        if (downPressed && playerRect.y + playerRect.height < HEIGHT) {
            playerRect.y += playerSpeed;
        }

        // Movimento dos meteoros
        for (int i = 0; i < meteorList.size(); i++) {
            Rectangle meteor = meteorList.get(i);
            int speed = meteorSpeeds.get(i);
            boolean isLife = meteorIsLife.get(i);
            boolean isDanger = meteorIsDanger.get(i);

            // Ajusta velocidade e tamanho
            if (isDanger) {
                meteor.y += meteorDangerSpeed;
                meteor.width = meteorDangerWidth;
                meteor.height = meteorDangerHeight;
            } else {
                meteor.y += speed;
                meteor.width = 40;
                meteor.height = 40;
            }

            // Saiu da tela
            if (meteor.y > HEIGHT) {
                meteor.y = random.nextInt(60) - 100;
                meteor.x = random.nextInt(WIDTH - meteor.width);
                meteorSpeeds.set(i, random.nextInt(7) + meteorSpeed);

                // Sorteia o tipo (8% perigo, 10% vida, 82% normal)
                int chance = random.nextInt(100);
                if (chance < 8) {
                    meteorIsDanger.set(i, true);
                    meteorIsLife.set(i, false);
                } else if (chance < 18) {
                    meteorIsDanger.set(i, false);
                    meteorIsLife.set(i, true);
                } else {
                    meteorIsDanger.set(i, false);
                    meteorIsLife.set(i, false);
                }

                // Só ganha ponto se desviou de meteoro normal ou perigoso
                if (!isLife) {
                    score++;
                    playSound(soundPoint);


                    // Checagem de vitória
                    if (score >= SCORE_TO_WIN) {
                        victory = true;
                        running = false;
                        if (music != null) music.stop();
                    }
                }
            }

            // Colisão
            if (meteor.intersects(playerRect)) {
                meteor.y = random.nextInt(60) - 100;
                meteor.x = random.nextInt(WIDTH - meteor.width);
                meteorSpeeds.set(i, random.nextInt(7) + meteorSpeed);

                // Reseta tipo
                meteorIsLife.set(i, false);
                meteorIsDanger.set(i, false);

                if (isLife) {
                    lives++;
                    playSound(soundPoint);
                } else if (isDanger) {
                    lives -= 2;
                    playSound(soundHit);
                } else {
                    lives--;
                    playSound(soundHit);
                }

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

        if (introScreen) {
            // Desenha a tela Intro
            g2d.setColor(new Color(20, 20, 20)); // Fundo escuro
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            g2d.setColor(WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));

            String title = "SPACE ESCAPE";
            FontMetrics fm = g2d.getFontMetrics(); // Pega métricas da fonte
            int x1 = (WIDTH - fm.stringWidth(title)) / 2; // Centraliza
            g2d.drawString(title, x1, HEIGHT / 2 - 50);
          
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            String subtitle = "Pressione ENTER para iniciar";
            fm = g2d.getFontMetrics(); // Pega métricas da fonte nova
            int x2 = (WIDTH - fm.stringWidth(subtitle)) / 2; // Centraliza
            g2d.drawString(subtitle, x2, HEIGHT / 2 + 20);

        } else if (gameOver || victory) {

            // Desenha a tela Game Over OU Vitória
            g2d.setColor(new Color(20, 20, 20));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
            g2d.setColor(WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));

            String endText;
            String scoreText = "Pontuação final: " + score;

            if (victory) {
                endText = "VOCÊ VENCEU! Pressione qualquer tecla para sair.";
            } else {
                endText = "Fim de jogo! Pressione qualquer tecla para sair.";
            }

            FontMetrics fm = g2d.getFontMetrics();
            int x1 = (WIDTH - fm.stringWidth(endText)) / 2;
            int x2 = (WIDTH - fm.stringWidth(scoreText)) / 2;

            g2d.drawString(endText, x1, HEIGHT / 2 - 20);
            g2d.drawString(scoreText, x2, HEIGHT / 2 + 20);

        } else {
            // Desenha fundo
            g2d.drawImage(background, 0, 0, null);

            // Desenha jogador
            g2d.drawImage(playerImg, playerRect.x, playerRect.y, null);

            // Desenha meteoros (a lógica complexa de tipos está na main)
            for (int i = 0; i < meteorList.size(); i++) { // <-- CORRIGIDO: Usa índice 'i'
                Rectangle meteor = meteorList.get(i);

                // Decide qual imagem desenhar
                if (meteorIsDanger.get(i)) {
                    g2d.drawImage(meteorDangerImg, meteor.x, meteor.y, null);
                } else if (meteorIsLife.get(i)) {
                    g2d.drawImage(meteorLifeImg, meteor.x, meteor.y, null);
                } else {
                    g2d.drawImage(meteorImg, meteor.x, meteor.y, null);
                }
            }

            // Desenha pontuação e vidas
            g2d.setColor(WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("Pontos: " + score + "   Vidas: " + lives, 10, 30);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver || victory) {
            System.exit(0);
        }
        if (introScreen) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                introScreen = false;
                timer.start();
            }
            return;
        }
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
        if (key == KeyEvent.VK_UP) {
            upPressed = true;
        }
        if (key == KeyEvent.VK_DOWN) {
            downPressed = true;
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
        if (key == KeyEvent.VK_UP) {
            upPressed = false;
        }
        if (key == KeyEvent.VK_DOWN) {
            downPressed = false;
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