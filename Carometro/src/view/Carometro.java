package view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import model.DAO;
import utils.Validador;
import javax.swing.JScrollPane;
import javax.swing.JList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import java.awt.Desktop;

public class Carometro extends JFrame {
	DAO dao = new DAO();
	private Connection con;

	private PreparedStatement pst;
	private ResultSet rs;

	private FileInputStream fis;
	private int tamanho;

	private boolean fotoCarregada = false;

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel lblStatus;
	private JLabel lblData;
	private JLabel lblRE;
	private JLabel lblNomeDoEstudante;
	private JTextField textNome;
	private JTextField textRE;
	private JLabel lblFoto;
	private JButton btnAdicionar;
	private JButton btnReset;
	private JButton btnBuscar;
	private JScrollPane scrollPaneLista;
	private JList<String> listNomes;
	private JButton btnEditar;
	private JButton btnExcluir;
	private JButton btnCarregar;
	private JLabel lblLupa;
	private JButton btnSobre;
	private JButton btnPDF;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Carometro frame = new Carometro();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Carometro() {

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				status();
				setarData();
			}
		});

		setResizable(false);
		setTitle("Carômetro");
		setIconImage(Toolkit.getDefaultToolkit().getImage(Carometro.class.getResource("/img/icamera.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 360);
		contentPane = new JPanel();
		contentPane.setForeground(SystemColor.desktop);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		scrollPaneLista = new JScrollPane();
		scrollPaneLista.setBorder(null);
		scrollPaneLista.setVisible(false);
		scrollPaneLista.setBounds(140, 84, 170, 122);
		contentPane.add(scrollPaneLista);

		listNomes = new JList<String>();

		listNomes.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buscarNome();
			}
		});

		listNomes.setBorder(null);
		scrollPaneLista.setViewportView(listNomes);

		JPanel panel = new JPanel();
		panel.setBackground(SystemColor.activeCaptionBorder);
		panel.setBounds(0, 281, 626, 42);
		contentPane.add(panel);
		panel.setLayout(null);

		lblStatus = new JLabel("");
		lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dboff.png")));
		lblStatus.setBounds(560, 5, 32, 32);
		panel.add(lblStatus);

		lblData = new JLabel("");
		lblData.setBounds(10, 3, 347, 34);
		panel.add(lblData);
		lblData.setForeground(new Color(255, 255, 255));

		lblRE = new JLabel("Registro do estudante:");
		lblRE.setFont(new Font("Arial", Font.PLAIN, 12));
		lblRE.setBounds(10, 23, 132, 29);
		contentPane.add(lblRE);

		textRE = new JTextField();
		textRE.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		textRE.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				String caracteres = "0123456789";

				if (!caracteres.contains(e.getKeyChar() + "")) {
					e.consume();
				}
			}
		});

		textRE.setBounds(140, 28, 108, 18);
		contentPane.add(textRE);
		textRE.setColumns(10);

		textRE.setDocument(new Validador(6));

		lblNomeDoEstudante = new JLabel("Nome do estudante:");
		lblNomeDoEstudante.setFont(new Font("Arial", Font.PLAIN, 12));
		lblNomeDoEstudante.setBounds(10, 62, 132, 29);
		contentPane.add(lblNomeDoEstudante);

		textNome = new JTextField();
		textNome.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		textNome.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				listarNomes();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					scrollPaneLista.setVisible(false);

					int confirma = JOptionPane.showConfirmDialog(null,
							"Estudante não cadastrado.\nDeseja cadastrar este estudante?", "Aviso",
							JOptionPane.YES_NO_OPTION);

					if (confirma == JOptionPane.YES_NO_OPTION) {
						textRE.setEditable(false);
						btnBuscar.setEnabled(false);
						btnCarregar.setEnabled(true);
						btnAdicionar.setEnabled(true);
						btnPDF.setEnabled(false);

					} else {
						reset();
					}
				}
			}
		});

		textNome.setColumns(10);
		textNome.setBounds(140, 67, 170, 18);
		contentPane.add(textNome);

		textNome.setDocument(new Validador(30));

		lblFoto = new JLabel("");
		lblFoto.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		lblFoto.setIcon(new ImageIcon(Carometro.class.getResource("/img/camera.png")));
		lblFoto.setBounds(358, 14, 256, 256);
		contentPane.add(lblFoto);

		btnCarregar = new JButton("Carregar Foto");
		btnCarregar.setEnabled(false);
		btnCarregar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				carregarFoto();
			}
		});

		btnCarregar.setFont(new Font("Arial", Font.PLAIN, 12));
		btnCarregar.setBackground(SystemColor.controlDkShadow);
		btnCarregar.setForeground(SystemColor.inactiveCaptionBorder);
		btnCarregar.setBounds(10, 102, 132, 25);
		contentPane.add(btnCarregar);

		btnAdicionar = new JButton("");
		btnAdicionar.setEnabled(false);
		btnAdicionar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				adicionar();
			}
		});

		btnAdicionar.setIcon(new ImageIcon(Carometro.class.getResource("/img/create.png")));
		btnAdicionar.setToolTipText("Adicionar");
		btnAdicionar.setBounds(10, 206, 64, 64);
		contentPane.add(btnAdicionar);

		btnReset = new JButton("");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});

		btnReset.setToolTipText("Limpar Campos");
		btnReset.setIcon(new ImageIcon(Carometro.class.getResource("/img/eraser.png")));
		btnReset.setBounds(285, 206, 64, 64);
		contentPane.add(btnReset);

		btnBuscar = new JButton("Buscar");
		btnBuscar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buscarRE(); // método
			}
		});

		btnBuscar.setForeground(SystemColor.inactiveCaptionBorder);
		btnBuscar.setBackground(SystemColor.controlDkShadow);
		btnBuscar.setBounds(258, 28, 81, 18);
		contentPane.add(btnBuscar);

		btnEditar = new JButton("");
		btnEditar.setEnabled(false);
		btnEditar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editar();
			}
		});

		btnEditar.setToolTipText("Editar");
		btnEditar.setIcon(new ImageIcon(Carometro.class.getResource("/img/update.png")));
		btnEditar.setBounds(78, 206, 64, 64);
		contentPane.add(btnEditar);

		btnExcluir = new JButton("");
		btnExcluir.setEnabled(false);
		btnExcluir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				excluir();
			}
		});
		btnExcluir.setToolTipText("Excluir");
		btnExcluir.setIcon(new ImageIcon(Carometro.class.getResource("/img/delete.png")));
		btnExcluir.setBounds(147, 206, 64, 64);
		contentPane.add(btnExcluir);

		lblLupa = new JLabel("");
		lblLupa.setIcon(new ImageIcon(Carometro.class.getResource("/img/search.png")));
		lblLupa.setBounds(315, 66, 24, 24);
		contentPane.add(lblLupa);

		btnSobre = new JButton("");
		btnSobre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Sobre sobre = new Sobre();
				sobre.setVisible(true);
			}
		});

		btnSobre.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSobre.setContentAreaFilled(false);
		btnSobre.setBorderPainted(false);
		btnSobre.setIcon(new ImageIcon(Carometro.class.getResource("/img/info.png")));
		btnSobre.setBounds(10, 145, 48, 48);
		contentPane.add(btnSobre);

		btnPDF = new JButton("");
		btnPDF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gerarPDF();
			}
		});

		btnPDF.setToolTipText("Gerar Lista de estudantes");
		btnPDF.setIcon(new ImageIcon(Carometro.class.getResource("/img/pdf.png")));
		btnPDF.setBounds(216, 206, 64, 64);
		contentPane.add(btnPDF);

		this.setLocationRelativeTo(null);

	}

	private void status() {
		try {
			con = dao.conectar();

			if (con == null) {
				lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dboff.png")));

			} else {
				lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dbon.png")));
			}

			con.close();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void setarData() {
		Date data = new Date();
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL);

		lblData.setText(formatador.format(data));
	}

	private void carregarFoto() {
		JFileChooser jfc = new JFileChooser();

		jfc.setDialogTitle("Selecionar Arquivos");
		jfc.setFileFilter(
				new FileNameExtensionFilter("Arquivos de imagens(*.PNG, *.JPG, *.JPEG)", "png", "jpg", "jpeg"));

		int resultado = jfc.showOpenDialog(this);

		if (resultado == JFileChooser.APPROVE_OPTION) {
			try {
				fis = new FileInputStream(jfc.getSelectedFile());
				tamanho = (int) jfc.getSelectedFile().length();
				Image foto = ImageIO.read(jfc.getSelectedFile()).getScaledInstance(lblFoto.getWidth(),
						lblFoto.getHeight(), Image.SCALE_SMOOTH);

				lblFoto.setIcon(new ImageIcon(foto));
				lblFoto.updateUI();

				fotoCarregada = true;

			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void adicionar() {
		if (textNome.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Digite o nome do estudante.");
			textNome.requestFocus();

		} else if (tamanho == 0) {
			JOptionPane.showMessageDialog(null, "Selecionar a foto");

		} else {
			String insert = "insert into estudantes(nome,foto) values(?,?)";

			try {
				con = dao.conectar();
				pst = con.prepareStatement(insert);

				pst.setString(1, textNome.getText());
				pst.setBlob(2, fis, tamanho);

				int confirma = pst.executeUpdate();

				if (confirma == 1) {
					JOptionPane.showMessageDialog(null, "Estudante cadastrado com sucesso!");

					reset();

				} else {
					JOptionPane.showMessageDialog(null, "Erro! Estudante não cadastrado!");
				}

				con.close();

			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void buscarRE() {
		if (textRE.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Digitar o registro do estudante");
			textRE.requestFocus();

		} else {
			String readRE = "select * from estudantes where re = ?";

			try {
				con = dao.conectar();
				pst = con.prepareStatement(readRE);
				pst.setString(1, textRE.getText());

				rs = pst.executeQuery();

				if (rs.next()) {
					textNome.setText(rs.getString(2));

					Blob blob = (Blob) rs.getBlob(3);

					byte[] img = blob.getBytes(1, (int) blob.length());

					BufferedImage imagem = null;

					try {
						imagem = ImageIO.read(new ByteArrayInputStream(img));

					} catch (Exception e) {
					}

					ImageIcon icone = new ImageIcon(imagem);

					Icon foto = new ImageIcon(icone.getImage().getScaledInstance(lblFoto.getWidth(),
							lblFoto.getHeight(), Image.SCALE_SMOOTH));

					lblFoto.setIcon(foto);

					textRE.setEnabled(false);
					btnBuscar.setEnabled(false);
					btnCarregar.setEnabled(true);
					btnEditar.setEnabled(true);
					btnExcluir.setEnabled(true);
					btnPDF.setEnabled(false);

				} else {
					int confirma = JOptionPane.showConfirmDialog(null,
							"Estudante não cadastrado!\nDeseja iniciar um novo cadastro?", "Aviso",
							JOptionPane.YES_NO_OPTION);

					if (confirma == JOptionPane.YES_NO_OPTION) {
						textRE.setEnabled(false);
						textRE.setText(null);
						btnBuscar.setEnabled(false);
						textNome.setText(null);
						textNome.requestFocus();
						btnCarregar.setEnabled(true);
						btnAdicionar.setEnabled(true);
					} else {
						reset();
					}
				}

				con.close();

			} catch (Exception e) {
				System.out.println(e);
			}

		}
	}

	private void listarNomes() {

		DefaultListModel<String> modelo = new DefaultListModel<>();

		listNomes.setModel(modelo);

		String readLista = "select * from estudantes where nome like '" + textNome.getText() + "%'" + "order by nome";

		try {
			con = dao.conectar();
			pst = con.prepareStatement(readLista);
			rs = pst.executeQuery();

			while (rs.next()) {
				scrollPaneLista.setVisible(true);
				modelo.addElement(rs.getString(2));

				if (textNome.getText().isEmpty()) {
					scrollPaneLista.setVisible(false);
				}
			}

			con.close();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void buscarNome() {
		int linha = listNomes.getSelectedIndex();

		if (linha >= 0) {
			String readNome = "select * from estudantes where nome like '" + textNome.getText() + "%'"
					+ "order by nome limit " + (linha) + ", 1";

			try {
				con = dao.conectar();
				pst = con.prepareStatement(readNome);
				rs = pst.executeQuery();

				while (rs.next()) {
					scrollPaneLista.setVisible(false);

					textRE.setText(rs.getString(1));
					textNome.setText(rs.getString(2));

					Blob blob = (Blob) rs.getBlob(3);

					byte[] img = blob.getBytes(1, (int) blob.length());

					BufferedImage imagem = null;

					try {
						imagem = ImageIO.read(new ByteArrayInputStream(img));

					} catch (Exception e) {
					}

					ImageIcon icone = new ImageIcon(imagem);

					Icon foto = new ImageIcon(icone.getImage().getScaledInstance(lblFoto.getWidth(),
							lblFoto.getHeight(), Image.SCALE_SMOOTH));

					lblFoto.setIcon(foto);

					textRE.setEnabled(false);
					btnBuscar.setEnabled(false);
					btnCarregar.setEnabled(true);
					btnEditar.setEnabled(true);
					btnExcluir.setEnabled(true);
					btnPDF.setEnabled(false);
				}

				con.close();

			} catch (Exception e) {
				System.out.println(e);
			}

		} else {
			scrollPaneLista.setVisible(false);
		}
	}

	private void editar() {
		if (textNome.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Digite o nome do estudante");
			textNome.requestFocus();

		} else {

			if (fotoCarregada == true) {

				String update = "update estudantes set nome=?, foto=? where re=?";
				try {
					con = dao.conectar();
					pst = con.prepareStatement(update);
					pst.setString(1, textNome.getText());
					pst.setBlob(2, fis, tamanho);
					pst.setString(3, textRE.getText());

					int confirma = pst.executeUpdate();

					if (confirma == 1) {
						JOptionPane.showMessageDialog(null, "Dados do estudante alterados!");
						reset();
					} else {
						JOptionPane.showMessageDialog(null, "Erro! Dados do estudante não alterado!");
					}

					con.close();

				} catch (Exception e) {
					System.out.println(e);
				}

			} else {

				String update = "update estudantes set nome=? where re=?";
				try {
					con = dao.conectar();
					pst = con.prepareStatement(update);
					pst.setString(1, textNome.getText());
					pst.setString(2, textRE.getText());

					int confirma = pst.executeUpdate();

					if (confirma == 1) {
						JOptionPane.showMessageDialog(null, "Dados do estudante alterados!");
						reset();
					} else {
						JOptionPane.showMessageDialog(null, "Erro! Dados do estudante não alterado!");
					}

					con.close();

				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
	}

	private void excluir() {
		int confirmaExcluir = JOptionPane.showConfirmDialog(null, "Confirma a exclusão deste estudante?", "Atenção!",
				JOptionPane.YES_NO_OPTION);

		if (confirmaExcluir == JOptionPane.YES_NO_OPTION) {
			String delete = "delete from estudantes where re=?";

			try {
				con = dao.conectar();
				pst = con.prepareStatement(delete);
				pst.setString(1, textRE.getText());

				int confirma = pst.executeUpdate();

				if (confirma == 1) {
					reset();
					JOptionPane.showMessageDialog(null, "Estudante excluído com sucesso!");
				}

				con.close();

			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void gerarPDF() {
		Document document = new Document();

		try {
			PdfWriter.getInstance(document, new FileOutputStream("estudantes.pdf"));

			document.open();
			Date data = new Date();
			DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL);

			document.add(new Paragraph(formatador.format(data)));
			document.add(new Paragraph("Listagem de estudantes"));
			document.add(new Paragraph(" "));

			PdfPTable tabela = new PdfPTable(3);
			PdfPCell col1 = new PdfPCell(new Paragraph("RE"));
			tabela.addCell(col1);

			PdfPCell col2 = new PdfPCell(new Paragraph("Nome"));
			tabela.addCell(col2);

			PdfPCell col3 = new PdfPCell(new Paragraph("Foto"));
			tabela.addCell(col3);

			String readLista = "select * from estudantes order by nome";

			try {
				con = dao.conectar();
				pst = con.prepareStatement(readLista);
				rs = pst.executeQuery();

				while (rs.next()) {
					tabela.addCell(rs.getString(1));
					tabela.addCell(rs.getString(2));
					Blob blob = (Blob) rs.getBlob(3);

					byte[] img = blob.getBytes(1, (int) blob.length());

					com.itextpdf.text.Image imagem = com.itextpdf.text.Image.getInstance(img);

					tabela.addCell(imagem);
				}

				con.close();

			} catch (Exception ex) {
				System.out.println(ex);
			}

			document.add(tabela);

		} catch (Exception e) {
			System.out.println(e);

		} finally {
			document.close();
		}

		try {
			Desktop.getDesktop().open(new File("estudantes.pdf"));

		} catch (Exception e2) {
			System.out.println(e2);
		}
	}

	private void reset() {
		scrollPaneLista.setVisible(false);

		textRE.setText(null);
		textNome.setText(null);
		lblFoto.setIcon(new ImageIcon(Carometro.class.getResource("/img/camera.png")));

		textNome.requestFocus();

		fotoCarregada = false;
		tamanho = 0;

		textRE.setEnabled(true);
		btnBuscar.setEnabled(true);
		btnCarregar.setEnabled(false);
		btnAdicionar.setEnabled(false);
		btnEditar.setEnabled(false);
		btnExcluir.setEnabled(false);
		btnPDF.setEnabled(true);
	}
}
