import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Minesweeper extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private class MineTile extends JButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int row;
		int col;
		boolean isMine;
		boolean isRevealed;
		boolean isFlagged;
		int adjacentMines;

		public MineTile(int row, int col) {
			this.row = row;
			this.col = col;
			this.isMine = false;
			this.isRevealed = false;
			this.isFlagged = false;
			this.adjacentMines = 0;
			setFont(new Font("Arial Unicode MS", Font.BOLD, 20)); // Make numbers clearer
			setFocusable(false);
			setMargin(new Insets(5, 5, 5, 5));
			setBackground(new Color(200, 200, 200)); // Light gray default
			setBorder(BorderFactory.createRaisedBevelBorder());

			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (gameOver) {
						return;
					}
					MineTile tile = (MineTile) e.getSource();

					// Left click
					if (e.getButton() == MouseEvent.BUTTON1) {
						if (tile.isMine) {
							revealMines();
						} else {
							checkMine(tile.row, tile.col);
						}
					}
					// Right click for flagging
					else if (e.getButton() == MouseEvent.BUTTON3) {
						if (!tile.isRevealed) {
							toggleFlag(tile);
						}
					}
				}
			});
		}

		protected void toggleFlag(Minesweeper.MineTile tile) {
			if (!tile.isRevealed) {
				if (tile.isFlagged) {
					tile.unflag();
					mineCount++;
				} else {
					tile.flag();
					mineCount--;
				}
				mineCountLabel.setText("Mines: " + mineCount);
			}

		}

		public void reveal() {
			isRevealed = true;
			setEnabled(false);
			setBorder(BorderFactory.createLoweredBevelBorder());
			if (isMine) {
				setText("💣");
				setForeground(Color.RED);
			} else if (adjacentMines > 0) {
				setText(Integer.toString(adjacentMines));
				setForeground(getNumberColor(adjacentMines));
			} else {
				setForeground(new Color(230, 230, 230)); // Lighter gray for reveal emty tiles
			}
		}

		public void flag() {
			isFlagged = true;
			setText("🚩");
			setForeground(Color.ORANGE);
		}

		public void unflag() {
			isFlagged = false;
			setText("");
		}
	}

	private int tileSize = 40;
	private int numRows = 15;
	private int numCols = numRows;
	private int boardWidth = numCols * tileSize;
	private int boardHeight = numRows * tileSize + 50;

	private JLabel mineCountLabel;
	private JLabel messageLabel;
	private JPanel infoPanel;
	private JPanel boardPanel;

	private int mineCount = 30;
	private MineTile[][] board = new MineTile[numRows][numCols];
	private ArrayList<MineTile> mineList;
	private Random random = new Random();

	private int tilesClicked = 0; // goal is to click all tiles except the ones containing mines
	private boolean gameOver = false;

	private JMenuBar menuBar;
	private JMenu gameMenu;
	private JMenuItem newGameItem;
	private JMenuItem settingsItem;

	public Minesweeper() {
		super("Minesweeper");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		menuBar = new JMenuBar();
		gameMenu = new JMenu("Game");
		newGameItem = new JMenuItem("New Game");
		settingsItem = new JMenuItem("Settings");

		newGameItem.addActionListener(this);
		settingsItem.addActionListener(this);

		gameMenu.add(newGameItem);
		gameMenu.addSeparator();
		gameMenu.add(settingsItem);
		menuBar.add(gameMenu);
		setJMenuBar(menuBar);

		mineCountLabel = new JLabel();
		mineCountLabel.setFont(new Font("Arial", Font.BOLD, 20));
		mineCountLabel.setHorizontalAlignment(JLabel.LEFT);
		mineCountLabel.setText("Minesweeper: " + Integer.toString(mineCount));

		messageLabel = new JLabel("Good Luck!", SwingConstants.CENTER);
		messageLabel.setFont(new Font("Arial", Font.ITALIC, 18));

		infoPanel = new JPanel(new BorderLayout());
		infoPanel.add(mineCountLabel, BorderLayout.WEST);
		infoPanel.add(messageLabel, BorderLayout.CENTER);
		infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Add padding to info panel
		add(infoPanel, BorderLayout.NORTH);

		boardPanel = new JPanel();
		boardPanel.setLayout(new GridLayout(numRows, numCols));
		add(boardPanel, BorderLayout.CENTER);

		initializeBoard();
		setSize(boardWidth, boardHeight);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	private void calculatedAdjacentMines() {
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				if (!board[row][col].isMine) {
					board[row][col].adjacentMines = countAdjacentMines(row, col);
				}
			}
		}
	}

	private int countAdjacentMines(int row, int col) {
		int count = 0;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int neighborRow = row + i;
				int neighborCol = col + j;
				if (neighborRow >= 0 && neighborRow < numRows && neighborCol >= 0 && neighborCol < numCols) {
					if (board[neighborRow][neighborCol].isMine) {
						count++;
					}
				}
			}
		}
		return count;
	}

	private void initializeBoard() {
		boardPanel.removeAll();
		boardPanel.setLayout(new GridLayout(numRows, numCols));
		board = new MineTile[numRows][numCols];
		tilesClicked = 0;
		gameOver = false;
		messageLabel.setText("Good Luck!");
		mineCountLabel.setText("Mines: " + mineCount);
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				MineTile tile = new MineTile(row, col);
				board[row][col] = tile;
				boardPanel.add(tile);
			}
		}
		setMines();
		calculatedAdjacentMines();
		boardPanel.revalidate();
		boardPanel.repaint();
	}

	public Color getNumberColor(int num) {
		return switch (num) {
		case 1 -> new Color(0, 0, 255); // Blue
		case 2 -> new Color(0, 128, 0); // Green
		case 3 -> new Color(255, 0, 0); // Red
		case 4 -> new Color(0, 0, 128); // Dark Blue
		case 5 -> new Color(128, 0, 0); // Maroon
		case 6 -> new Color(0, 128, 128); // Teal
		case 7 -> new Color(0, 0, 0); // Black
		case 8 -> new Color(128, 128, 128); // Gray
		default -> Color.BLACK;
		};
	}

	protected void checkMine(int row, int col) {
		if (row < 0 || row >= numRows || col < 0 || col >= numCols || board[row][col].isRevealed) {
			return;
		}
		MineTile tile = board[row][col];
		tile.reveal();
		tilesClicked++;

		if (tile.adjacentMines == 0) {
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					checkMine(row + i, col + j);
				}
			}
		}
		if (tilesClicked == numRows * numCols - mineList.size()) {
			gameOver = true;
			mineCountLabel.setText("Mines Cleared!");
		}
	}

	protected void revealMines() {
		gameOver = true;
		messageLabel.setText("Game Over!");
		for (MineTile mine : mineList) {
			mine.reveal();
		}
		// Optionally disable all tiles
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				board[row][col].setEnabled(false);
			}
		}
	}

	private void setMines() {
		mineList = new ArrayList<Minesweeper.MineTile>();
		int minesToPlace = mineCount;
		while (minesToPlace > 0) {
			int row = random.nextInt(numRows); // 0-7
			int col = random.nextInt(numCols); // 0-7
			MineTile tile = board[row][col];
			if (!mineList.contains(tile)) {
				tile.isMine = true;
				mineList.add(tile);
				minesToPlace--;
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == newGameItem) {
			initializeBoard();
		} else if (e.getSource() == settingsItem) {
			showSettingsDialog();
		}
	}

	private void showSettingsDialog() {
		JPanel panel = new JPanel(new GridLayout(0, 2));
		JTextField rowsField = new JTextField(Integer.toString(numRows));
		JTextField colsField = new JTextField(Integer.toString(numCols));
		JTextField minesField = new JTextField(Integer.toString(mineCount));

		panel.add(new JLabel("Row:"));
		panel.add(rowsField);
		panel.add(new JLabel("Columns: "));
		panel.add(colsField);
		panel.add(new JLabel("Mine: "));
		panel.add(minesField);

		int result = JOptionPane.showConfirmDialog(this, panel, "Game Settings", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
			try {
				int newRows = Integer.parseInt(rowsField.getText());
				int newCols = Integer.parseInt(colsField.getText());
				int newMines = Integer.parseInt(minesField.getText());

				if (newRows > 0 && newCols > 0 && newMines >= 0 && newMines < newRows * newCols) {
					numRows = newRows;
					numCols = newCols;
					mineCount = newMines;
					boardWidth = numCols * tileSize;
					boardHeight = numRows * tileSize + 50;
					setSize(boardWidth, boardHeight);
					initializeBoard();
					setLocationRelativeTo(null); // Keep centered after resize
				} else {
					JOptionPane.showMessageDialog(this,
							"Invalid settings. Please ensure positive dimensions and a valid number of mines.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Invalid input. Please enter numbers only.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
