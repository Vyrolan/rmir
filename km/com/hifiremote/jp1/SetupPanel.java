package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.border.*;
import javax.swing.*;
import java.text.*;
import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import info.clearthought.layout.*;

public class SetupPanel
  extends KMPanel
  implements ActionListener, DocumentListener, FocusListener, ItemListener
{
  public SetupPanel( DeviceUpgrade deviceUpgrade, Vector protocols )
  {
    super( deviceUpgrade );

    protocolHolder = new JPanel( new BorderLayout());
    Border border = BorderFactory.createTitledBorder( "Protocol Parameters" );
    protocolHolder.setBorder( border );

    Insets insets = border.getBorderInsets( protocolHolder );
    double bt = insets.top;
    double bl = insets.left + 10;
    double br = insets.right;
    double bb = insets.bottom;
    double b = 10;       // space around border
    double i = 5;        // space between rows
    double v = 20;       // space between groupings
    double c = 10;       // space between columns
    double f = TableLayout.FILL;
    double p = TableLayout.PREFERRED;
    double size[][] =
    {
      { b, bl, p, b, p, br, c, f, b },                     // cols
      { b, p, v, p, i, p, v, bt, p, bb, f, b }         // rows
    };
    tl = new TableLayout( size );
    setLayout( tl );

    JLabel label = new JLabel( "Setup Code:", SwingConstants.RIGHT );
    add( label, "2, 1" );

//    IntFormatter nf = new IntFormatter( 0, 2047, 4 );
//    nf.setAllowsInvalid( false );
//    nf.setOverwriteMode( true );
//    setupCode = new JFormattedTextField( nf );
    setupCode = new JTextField();
    setupCode.addFocusListener( this );
    setupCode.setInputVerifier( new IntVerifier( 0, 2047 ));
    setupCode.addActionListener( this );
    label.setLabelFor( setupCode );
//    setupCode.setFocusLostBehavior( JFormattedTextField.COMMIT_OR_REVERT );
    setupCode.setToolTipText( "Enter the desired setup code (between 0 and 2047) for the device upgrade." );

    add( setupCode, "4, 1" );

    JPanel notesPanel = new JPanel( new BorderLayout());
    notes = new JTextArea();
    notes.setToolTipText( "Enter any notes about this device upgrade." );
    notesPanel.setBorder( BorderFactory.createTitledBorder( "Upgrade Notes" ));
    notesPanel.add( new JScrollPane( notes ), BorderLayout.CENTER );
    notes.getDocument().addDocumentListener( this );
    add( notesPanel, "7, 1, 7, 9" );

    label = new JLabel( "Protocol:", SwingConstants.RIGHT );
    add( label, "2, 3" );

    protocolList = new JComboBox( protocols );
    protocolList.addActionListener( this );
    label.setLabelFor( protocolList );
    protocolList.setToolTipText( "Select the protocol to be used for this device upgrade from the drop-down list." );
    add( protocolList, "4, 3" );

    label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    add( label, "2, 5" );

    protocolID = new JTextField();
    label.setLabelFor( protocolID );
    protocolID.setEditable( false );
    protocolID.setToolTipText( "This is the protocol ID that corresponds to the selected protocol." );
    add( protocolID, "4, 5" );

    add( protocolHolder, "1, 7, 5, 9" );
    label = new JLabel( "Fixed Data:", SwingConstants.RIGHT );
    add( label, "2, 8" );

    fixedData = new JTextField();
    fixedData.setEditable( false );
    add( fixedData, "4, 8" );

    notesPanel = new JPanel( new BorderLayout());
    protocolNotes = new JTextArea();
    protocolNotes.setBackground( label.getBackground());
    protocolNotes.setToolTipText( "Notes about the selected protocol." );
    protocolNotes.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    notesPanel.setBorder( BorderFactory.createTitledBorder( "Protocol Notes" ));
    notesPanel.add( new JScrollPane( protocolNotes ), BorderLayout.CENTER );
    protocolNotes.setEditable( false );
    add( notesPanel, "1, 10, 7, 10" );
  }

  public void protocolsLoaded( Vector protocols )
  {
    protocolList.setModel( new DefaultComboBoxModel( protocols ));
  }

  public void update()
  {
    updateInProgress = true;
//    setupCode.setValue( new Integer( deviceUpgrade.getSetupCode()));
    setupCode.setText( nf.format( deviceUpgrade.getSetupCode()));
    Protocol p = deviceUpgrade.getProtocol();
    protocolList.setSelectedItem( p );
    notes.setText( deviceUpgrade.getNotes());
    fixedData.setText( p.getFixedData().toString());
    updateInProgress = false;
  }

  public void updateFixedData()
  {
    Protocol p = getProtocol();
    p.initializeParms();
    fixedData.setText( p.getFixedData().toString());
  }

  // ActionListener Methods
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();

    if ( source == protocolList )
    {
      Protocol protocol = ( Protocol )protocolList.getSelectedItem();
      if ( protocol != null && ( currProtocol != protocol ))
      {
        if ( currProtocol != null && !updateInProgress )
          currProtocol.convertFunctions( deviceUpgrade.getFunctions(), protocol );
        currProtocol = protocol;
        protocolID.setText( protocol.getID().toString());
        JViewport vp = ( JViewport )protocolNotes.getParent();
        vp.setViewPosition( new Point( 0, 0 ));
        deviceUpgrade.setProtocol( protocol );
        if ( parameters != null )
        {
          for ( int i = 0; i < parameters.length; i++ )
          {
            parameters[ i ].removeListener( this );

            remove( parameters[ i ].getLabel());
            remove( parameters[ i ].getComponent());
            tl.deleteRow( 8 );
            tl.deleteRow( 8 );
          }
        }
        parameters = protocol.getDeviceParameters();
        if ( parameters != null )
        {
          int row = 8;
          for ( int i = 0; i < parameters.length; i++ )
          {
            parameters[ i ].addListener( this );
            
            tl.insertRow( row, TableLayout.PREFERRED );
            add( parameters[ i ].getLabel(), "2, " + row );
            add( parameters[ i ].getComponent() , "4, " + row );
            row++;
            tl.insertRow( row++, 5 );
          }
          TableLayoutConstraints tlc = tl.getConstraints( protocolHolder );
          remove( protocolHolder );
          add( protocolHolder, tlc );
        }
        fixedData.setText( protocol.getFixedData().toString());
        revalidate();
        protocolNotes.setText( protocol.getNotes());
        protocolNotes.revalidate();
      }
    }
    else if ( source == setupCode )
      updateSetupCode();
    else // must be a protocol parameter
      updateFixedData();
  }

  public Protocol getProtocol()
  {
    return ( Protocol )protocolList.getSelectedItem();
  }

  public void commit()
  {
    Protocol p = getProtocol();
    deviceUpgrade.setProtocol( p );

    for ( int i = 0; i < parameters.length; i++ )
      parameters[ i ].commit();

    p.updateFunctions( deviceUpgrade.getFunctions());
  }

  private void updateNotes()
  {
    deviceUpgrade.setNotes( notes.getText());
  }

  private void updateSetupCode()
  {
    int val = Integer.parseInt( setupCode.getText());
    setupCode.setText( nf.format( val ));
    deviceUpgrade.setSetupCode( val );
  }

  private void docChanged( DocumentEvent e )
  {
    Document doc = e.getDocument();
    if ( doc == notes.getDocument() )
      updateNotes();
    else if ( doc == setupCode.getDocument())
      updateSetupCode();
    else
      updateFixedData();

  }

  // DocumentListener
  public void changedUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  public void insertUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  public void removeUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  // FocusListener
  public void focusGained( FocusEvent e )
  {
    JTextComponent tc = ( JTextComponent )e.getSource();
    tc.selectAll();
  }
  
  public void focusLost( FocusEvent e )
  {
    JTextComponent tc = ( JTextComponent )e.getSource();
    if ( tc == setupCode )
      updateSetupCode();
    else
      updateFixedData();
  }

  // ItemListener
  public void itemStateChanged( ItemEvent e )
  {
    updateFixedData();
  }

//  private JFormattedTextField setupCode = null;
  private JTextField setupCode = null;
  private JRadioButton useEFC = null;
  private JRadioButton useOBC = null;
  private JComboBox protocolList = null;
  private JTextField protocolID = null;
  private JTextArea notes = null;
  private JPanel protocolHolder = null;
  private JTextField fixedData = null;
  private JTextArea protocolNotes = null;
  private DeviceParameter[] parameters = null;
  private Protocol currProtocol = null;
  private TableLayout tl;
  private boolean updateInProgress = false;
  private static DecimalFormat nf = new DecimalFormat( "0000" );
}

