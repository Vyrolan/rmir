package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import com.hifiremote.jp1.io.IO;

// TODO: Auto-generated Javadoc
/**
 * The Class RawDataDialog.
 */
public class RawDataDialog extends JDialog implements ActionListener
{
  /**
   * Instantiates a new raw data panel.
   */
  public RawDataDialog( RemoteMaster owner )
  {
    super( owner, "Download Raw", true );
    this.owner = owner;
    setLocationRelativeTo( owner );

    ( ( JPanel )getContentPane() ).setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

    model = new RawDataTableModel();
    JP1Table table = new JP1Table( model );
    table.initColumns( model );
    table.setGridColor( Color.lightGray );
    table.getTableHeader().setResizingAllowed( false );
    table.setDefaultRenderer( UnsignedByte.class, byteRenderer );
    JScrollPane scrollPane = new JScrollPane( table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    table.setPreferredScrollableViewportSize( d );
    add( scrollPane, BorderLayout.CENTER );

    JPanel statusPanel = new JPanel( new BorderLayout() );
    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.TRAILING ) );
    statusPanel.add( buttonPanel, BorderLayout.LINE_END );
    
    downloadLabel.setText( "DOWNLOADING..." );
    downloadLabel.setForeground( Color.RED );
    downloadLabel.setFont( downloadLabel.getFont().deriveFont( Font.BOLD ) );
    downloadLabel.setVisible( false );
    statusPanel.add( downloadLabel, BorderLayout.LINE_START );

    downloadButton.addActionListener( this );
    buttonPanel.add( downloadButton );

    setBaselineButton.addActionListener( this );
    setBaselineButton.setEnabled( false );
    buttonPanel.add( setBaselineButton );

    saveButton.addActionListener( this );
    saveButton.setEnabled( false );
    buttonPanel.add( saveButton );

    cancelButton.addActionListener( this );
    buttonPanel.add( cancelButton );

    add( statusPanel, BorderLayout.PAGE_END );

    pack();
    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );
  }

  private class RawDownloadTask extends SwingWorker< Void, Void >
  {
    @Override
    protected Void doInBackground() throws Exception
    {
      IO io = owner.getOpenInterface();
      if ( io == null )
      {
        JOptionPane.showMessageDialog( owner, "No remotes found!" );
        return null;
      }
      System.err.println( "Interface opened successfully" );
      baseAddress = io.getRemoteEepromAddress();
      System.err.println( "Base address = $" + Integer.toHexString( baseAddress ).toUpperCase() );
      
      signature = RemoteMaster.getIOsignature( io, baseAddress );
      int buffSize = io.getRemoteEepromSize();
      System.err.println( "Initial buffer size  = $" + Integer.toHexString( buffSize ).toUpperCase() );
      if ( buffSize <= 0 )
      {
        if ( buffer == null )
        {
          String[] choices =
          {
              "1K", "2K", "4K", "8K"
          };
          String choice = ( String )JOptionPane.showInputDialog( owner,
              "Select the number of bytes to download from the remote.", "Raw Download Size",
              JOptionPane.QUESTION_MESSAGE, null, choices, choices[ 1 ] );
          if ( choice == null )
          {
            return null;
          }
          buffSize = Integer.parseInt( choice.substring( 0, 1 ) ) * 1024;
        }
        else
        {
          buffSize = buffer.length;
        }
      }
      System.err.println( "Final buffer size  = $" + Integer.toHexString( buffSize ).toUpperCase() );
      buffer = new short[ buffSize ];
      int count = io.readRemote( baseAddress, buffer );
      System.err.println( "Number of bytes read  = $" + Integer.toHexString( count ).toUpperCase() );
      io.closeRemote();
      System.err.println( "Ending raw download" );
      model.set( buffer, baseAddress );
      setBaselineButton.setEnabled( true );
      saveButton.setEnabled( true );
      downloadLabel.setVisible( false );
      return null;
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMPanel#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener( PropertyChangeListener l )
  {
    if ( ( model != null ) && ( l != null ) )
      model.addPropertyChangeListener( l );
  }

  /** The model. */
  RawDataTableModel model = null;

  /** The byte renderer. */
  UnsignedByteRenderer byteRenderer = new UnsignedByteRenderer();

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    if ( source == downloadButton )
    {
      System.err.println( "Starting raw download" );
      downloadLabel.setVisible( true );
      ( new RawDownloadTask() ).execute();
    }
    else if ( source == setBaselineButton )
    {
      byteRenderer.setSavedData( buffer );
    }
    else if ( source == saveButton )
    {
      RMFileChooser chooser = owner.getFileChooser();
      // Only use 4 chars of signature as remotes with a 4-char signature may not
      // have the remaining 4 junk chars being valid in a filename.
      File rawFile = new File( signature.substring( 0, 4 ) + ".ir" );
      chooser.setSelectedFile( rawFile );
      int returnVal = chooser.showSaveDialog( this );
      if ( returnVal == RMFileChooser.APPROVE_OPTION )
      {
        rawFile = chooser.getSelectedFile();
        int rc = JOptionPane.YES_OPTION;
        if ( rawFile.exists() )
          rc = JOptionPane.showConfirmDialog( this, rawFile.getName() + " already exists.  Do you want to replace it?",
              "Replace existing file?", JOptionPane.YES_NO_OPTION );

        if ( rc != JOptionPane.YES_OPTION )
          return;

        try
        {
          PrintWriter pw = new PrintWriter( new BufferedWriter( new FileWriter( rawFile ) ) );

          Hex.print( pw, buffer, baseAddress );

          pw.close();
        }
        catch ( IOException ioe )
        {
          JOptionPane.showMessageDialog( owner, "Error writing to " + rawFile );
        }
      }
    }
    else if ( source == cancelButton )
    {
      setVisible( false );
      dispose();
    }
  }

  private RemoteMaster owner = null;
  private String signature = null;
  private short[] buffer = null;
  private int baseAddress = 0;

  private JButton downloadButton = new JButton( "Download" );
  private JButton setBaselineButton = new JButton( "Set Baseline" );
  private JButton saveButton = new JButton( "Save" );
  private JButton cancelButton = new JButton( "Cancel" );
  
  private JLabel downloadLabel = new JLabel();
}
