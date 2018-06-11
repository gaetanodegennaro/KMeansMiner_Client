import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * 
 * Client che si connette al rispettivo server ed estendendo {@link javax.swing.JFrame} fornisce un interfaccia per l'utente,
 * da cui è possibile selezionare la modalità  di raccolta dei dati per l'apprendimento dei cluster.
 * Scambia informazioni col server e in base agli input forniti stampa a video il risultato dell'elaborazione eseguita dal server
 * dell'algoritmo KMeans.
 * 
 * @author De Gennaro Gaetano, Farinola Francesco
 */
public class KMeans extends JFrame
{
	/**
	 * Stream di output che permette di inviare richieste al server.
	 */
	private ObjectOutputStream out;
	/**
	 * Stream di input che permette di ricevere informazioni dal server.
	 */
	private ObjectInputStream in;
	
	/**
	 * Richiama il metodo {@link #init()} che inizializza gli stream {@link #in} e {@link #out} per le comunicazioni con il server,
	 * e inizializza i componenti grafici.  
	 */
	public KMeans()
	{
		init();
	}
	
	/**
	 * Inizializza i componenti grafici, istanziando un {@link #TabbedPane}.<br />
	 * Avvia la connessione con il server: iInizializza gli stream {@link #in} e {@link #out} per la comunicazione con il server.<br />
	 * Chiude l'applicazione in caso di fallimento durante l'apertura della connessione.
	 * 
	 * @see JTabbedPane
	 * @see Socket
	 */
	private void init()
	{
		TabbedPane tb = new TabbedPane();
		this.getContentPane().add(tb);
		setTitle("KMeansMiner - de Gennaro Gaetano & Farinola Francesco");
		pack();
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		setResizable(false);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter()
		{
		    @Override
		    public void windowClosing(WindowEvent e)
		    {
		        int confirm = JOptionPane.showOptionDialog(null, "Are you sure to close application?", 
		        		"Exit confirmation", JOptionPane.YES_NO_OPTION, 
		        		JOptionPane.QUESTION_MESSAGE, null, null, null);
		        if (confirm == 0)
		        {
		        	try
		        	{
		        		out.close();
			        	in.close();
		        	}
		        	catch(IOException ex) {JOptionPane.showMessageDialog(null, "Error during closing connection.", "Error", JOptionPane.ERROR_MESSAGE);}
		        	finally {System.exit(0);}
		        }
		    }
		});
		
		setVisible(true);
		
		final String ip="127.0.0.1";
		final int port = new Integer("8080").intValue();
		try
		{
			InetAddress addr = InetAddress.getByName(ip); //ip
			Socket socket = new Socket(addr, port); //Port
			System.out.println(socket);
			
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());	; // stream con richieste del client
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(null, "Unable to connect to server.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	public static void main(String args[])
	{
		new KMeans();
	}
	
	/**
	 * Estende JPanel.<br />
	 * Rappresenta il {@link javax.swing.JPanel} principale dell'app, atto a contenere un {@link javax.swing.JTabbedPane} composto da
	 * due {@link #JPanelCluster} che forniscono le componenti adeguate per l'utilizzo del software.
	 * 
	 * @author de Gennaro Gaetano, Farinola Francesco
	 * 
	 * @see javax.swing.JPanel
	 * @see JPanelCluster
	 */
	private class TabbedPane extends JPanel
	{
		/**
		 * {@link #JPanelCluster} per l'utilizzo delle funzionalità su database.
		 */
		private JPanelCluster panelDB;
		
		/**
		 * {@link #JPanelCluster} per l'utilizzo delle funzionalità su file.
		 */
		private JPanelCluster panelFile;
		
		/**
		 * Inizializza l'interfaccia utente mediante l'utilizzo di un {@link javax.swing.JTabbedPane} al quale vengono aggiunti
		 * {@link #panelDB} e {@link #panelFile}, opportunamente istanziati ed inizializzati.
		 */
		TabbedPane()
		{
			ActionListener listener1 = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						learningFromDBAction();
					}
					catch(ClassNotFoundException ex) {JOptionPane.showMessageDialog(null, ex.toString(), "Warning", JOptionPane.ERROR_MESSAGE);}
					catch(IOException ex) {JOptionPane.showMessageDialog(null, ex.toString(), "Warning", JOptionPane.ERROR_MESSAGE);}
					catch(ServerException ex) {JOptionPane.showMessageDialog(null, ex.toString(), "Warning", JOptionPane.ERROR_MESSAGE);}
				}
			};
			
			ActionListener listener2 = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						learningFromFileAction();
					}
					catch(ClassNotFoundException ex) {JOptionPane.showMessageDialog(null, ex.toString(), "Warning", JOptionPane.ERROR_MESSAGE);}
					catch(IOException ex) {JOptionPane.showMessageDialog(null, ex.toString(), "Warning", JOptionPane.ERROR_MESSAGE);}
					catch(ServerException ex) {JOptionPane.showMessageDialog(null, ex.toString(), "Warning", JOptionPane.ERROR_MESSAGE);}
				}
			};
			
			panelDB = new JPanelCluster("MINE", listener1);
			panelFile = new JPanelCluster("STORE FROM FILE", listener2);
			
			JTabbedPane tabbedPane = new JTabbedPane();
			
			tabbedPane.addTab("DB", new ImageIcon("img\\db.png"), panelDB, "");
			tabbedPane.addTab("FILE", new ImageIcon("img\\file.png"), panelFile, "");
			
			add(tabbedPane);
		}
		
		/**
		 * Utilizzato per la scoperta dei cluster a partire dalle informazioni presenti nella base di dati.
		 * Si avvale degli input forniti dall'utente nelle rispettive caselle di testo per ottenere il nome della tabella
		 * dalla quale attingere informazioni e il numero k di cluster.
		 * Avviene un controllo client-side sull'input di k, che impedisce l'inserimento di numeri negativi.
		 * 
		 * @throws SocketException sollevata quando si verifia un errore durante l'accesso al socket
		 * @throws IOException sollevata quando si verificano errori durante la lettura/scrittura di informazioni da/su server mediante gli stream 
		 * {@link #in} e {@link #out}
		 * @throws ClassNotFoundException sollevata quando si effettua il cast ad un tipo non risolvibile
		 * @throws ServerException sollevata quando su server si verifica un'eccezione grazie alla quale non è possibile portare a termine la richiesta
		 * 
		 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/net/SocketException.html">SocketException</a>
		 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/io/IOException.html">IOException</a>
		 * @see ServerException
		 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/ClassNotFoundException.html">ClassNotFoundException</a>
		 */
		private void learningFromDBAction() throws SocketException, IOException, ClassNotFoundException, ServerException
		{
			int k;
			
			try
			{
				k = new Integer(panelDB.kText.getText()).intValue();
			}
			catch(NumberFormatException e)
			{
				JOptionPane.showMessageDialog(null, "Invalid k value.\n"+e.toString(), "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			out.writeObject(0);
			out.writeObject(panelDB.tableText.getText());
			String result = (String)in.readObject();
			if(!result.equals("OK")) throw new ServerException(result);
			
			out.writeObject(1);
			out.writeObject(k);
			result = (String)in.readObject();
			if(!result.equals("OK")) throw new ServerException(result);
			
			panelDB.clusterOutput.setText("Iterations number: "+in.readObject()+"\n");
			panelDB.clusterOutput.append((String)in.readObject());
			
			out.writeObject(2);
			
			result = (String)in.readObject();
			if(!result.equals("OK")) throw new ServerException(result);
			else JOptionPane.showMessageDialog(null, "Operation completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
		}
		
		/**
		 * Utilizzato per la la lettura di un file presente su server.
		 * Si avvale degli input forniti dall'utente nelle rispettive caselle di testo per ottenere (mediante concatenazione)
		 * il nome del file dal quale effettuare l'operazione di lettura.
		 * 
		 * @throws SocketException sollevata quando si verifia un errore durante l'accesso al socket
		 * @throws IOException sollevata quando si verificano errori durante la lettura/scrittura di informazioni da/su server mediante gli stream 
		 * {@link #in} e {@link #out}
		 * @throws ClassNotFoundException sollevata quando si effettua il cast ad un tipo non risolvibile
		 * @throws ServerException sollevata quando su server si verifica un'eccezione grazie alla quale non è possibile portare a termine la richiesta
		 * 
		 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/net/SocketException.html">SocketException</a>
		 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/io/IOException.html">IOException</a>
		 * @see ServerException
		 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/ClassNotFoundException.html">ClassNotFoundException</a>
		 */
		private void learningFromFileAction() throws SocketException,IOException, ClassNotFoundException, ServerException
		{
			out.writeObject(3);
			out.writeObject(panelFile.tableText.getText());
			out.writeObject(Integer.parseInt(panelFile.kText.getText()));
			String result = (String)in.readObject();
			
			if(!result.equals("OK")) throw new ServerException(result);
			panelFile.clusterOutput.setText((String)in.readObject());
			JOptionPane.showMessageDialog(null, "Operation completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE); 
			
		}
		
		/**
		 * Estende {@link javax.swing.JPanel}.<br />
		 * Pannello composto da una serie di componenti grafiche atte al semplice e corretto utilizzo del software:
		 * <li>Una {@link javax.swing.JTextField} per l'input del nome della tabella;</li>
		 * <li>Una {@link javax.swing.JTextField} per l'input del numero k di cluster;</li>
		 * <li>Una {@link javax.swing.JTextArea} atta a contenere l'output dell'elaborazione ricevuta dal server;</li>
		 * <li>Un {@link javax.swing.JButton} per l'invio della richiesta al server.</li>
		 * 
		 * @author de Gennaro Gaetano, Farinola Francesco
		 * @see javax.swing.JPanel
		 * @see javax.swing.JTextField
		 * @see javax.swing.JTextArea
		 * @see javax.swing.JButton
		 *
		 */
		private class JPanelCluster extends JPanel
		{
			/**
			 * {@link javax.swing.JTextField} utilizzata per l'input del nome della tabella.
			 */
			private JTextField tableText=new JTextField(20);
			
			/**
			 * {@link javax.swing.JTextField} utilizzata per l'input del numero k di cluster.
			 */
			private JTextField kText=new JTextField(10);
			
			/**
			 * {@link javax.swing.JTextArea} utilizzata per visualizzare l'output dell'elaborazione ricevuta dal server.
			 */
			private JTextArea clusterOutput=new JTextArea(15,50);
			
			/**
			 * {@link javax.swing.JButton} utilizzato per l'invio della richiesta al server.
			 */
			private JButton executeButton;
			
			/**
			 * Inizializza e aggiunge tutti gli attributi al JPanel.
			 * 
			 * @param buttonName contiene il testo da visualizzare all'interno del {@link javax.swing.JPanel}.
			 * @param a ascoltatore da aggiungere a {@link #executeButton} per eseguire le opportune operazioni al click del componente.
			 */
			JPanelCluster(String buttonName, java.awt.event.ActionListener a)
			{
				this.setLayout(new BorderLayout());
				JPanel upPanel = new JPanel();
				JPanel centralPanel = new JPanel();
				JPanel downPanel = new JPanel();

				upPanel.add(new JLabel("Table : "));
				upPanel.add(this.tableText);
				upPanel.add(new JLabel("k: "));
				upPanel.add(this.kText);
				
				clusterOutput.setEditable(false);
				centralPanel.add(new JScrollPane(clusterOutput));
				
				executeButton = new JButton(buttonName);
				executeButton.addActionListener(a);
				downPanel.add(executeButton);
				
				add(upPanel, BorderLayout.PAGE_START);
				add(centralPanel, BorderLayout.CENTER);
				add(downPanel, BorderLayout.PAGE_END);
			}
		}
	}
}
