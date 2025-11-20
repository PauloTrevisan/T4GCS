# SPACE ESCAPE - Versão Final do Projeto

Space Escape é um jogo em Java onde o jogador controla uma nave espacial tentando sobreviver ao maior número possível de meteoros que caem do espaço.
Durante a partida, meteoros especiais podem:
- tirar duas vidas
- dar vida extra
- causar tremor de tela
- mudar a música conforme a fase
- ou até surgir o mega meteoro, extremamente perigoso

O objetivo é sobreviver e alcançar 100 pontos para vencer.

## Como Clonar e Executar

1. Clonar o repositório
`git clone <https://github.com/PauloTrevisan/T4GCS.git>`

2. Compilar (a partir da raiz do projeto)
`javac src/SpaceEscape.java`

3. Executar
`java -cp src SpaceEscape`

## Como Jogar 
- Setas esquerda/direita: mover horizontalmente
- Setas cima/baixo: mover verticalmente
- ENTER na tela inicial: começar
- Evite os meteoros: sobreviva o máximo possível
- Ao chegar em 100 pontos, você vence
- Se perder todas as vidas: Game Over
- Pressione qualquer tecla na tela final para sair

## Funcionalidades Implementadas (11 features)

### TELAS
	1. Tela de introdução (“Pressione ENTER para iniciar”)
	2. Tela final de vitória ou derrota

### NAVE
	3. Movimento vertical adicional
	4. Wraparound horizontal (teleporte nas bordas da tela)

### METEOROS
	5. Velocidades diferentes para cada meteoro
	6. Meteoro de perigo que tira duas vidas
	7. Mega Meteoro (grande, lento e muito perigoso)
	8. Meteoro de vida extra
	9. Sons personalizados para cada tipo de meteoro

### ESTILIZAÇÃO / EFEITOS
	10. Músicas diferentes para cada fase (fase 1, 2 e 3)
	11.	Tela treme ao colidir com meteoros

## Imagens e Sons
Todas as imagens e sons utilizados estão na pasta:
`/assets/sounds`
