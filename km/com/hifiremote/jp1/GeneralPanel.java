package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.hifiremote.jp1.GeneralFunction.RMIcon;

// TODO: Auto-generated Javadoc
/**
 * The Class GeneralPanel.
 */
public class GeneralPanel extends RMPanel implements ListSelectionListener, ActionListener, DocumentListener
{

  /**
   * Instantiates a new general panel.
   */
  public GeneralPanel()
  {
    deviceButtonPanel = new JPanel( new BorderLayout() );

    deviceButtonPanel.setBorder( BorderFactory.createTitledBorder( "Device Buttons" ) );

    deviceButtonTable = new JP1Table( deviceModel );
    deviceButtonTable.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    deviceButtonTable.getSelectionModel().addListSelectionListener( this );
    deviceButtonTable.initColumns( deviceModel );
    deviceButtonTable.addMouseListener( new MouseAdapter()
    {
      @Override
      public void mouseClicked( MouseEvent e )
      {
        if ( e.getClickCount() != 2 )
        {
          return;
        }
        int row = deviceButtonTable.getSelectedRow();
        if ( row == -1 )
        {
          return;
        }
        if ( !deviceButtonTable.isCellEditable( row, deviceButtonTable.columnAtPoint( e.getPoint() ) ) )
        {
          editUpgradeInRow( row );
        }
      }
    } );

    deviceButtonTable.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusGained( FocusEvent e )
      {
        activeTable = deviceButtonTable;
        setHighlightAction( deviceButtonTable );
      }
    } );

    activeTable = deviceButtonTable;
    deviceScrollPane = new JScrollPane( deviceButtonTable );
    deviceButtonPanel.add( deviceScrollPane, BorderLayout.CENTER );
    
    messageArea = new JTextArea();
    JLabel label = new JLabel();
    messageArea.setFont( label.getFont() );
    messageArea.setBackground( label.getBackground() );
    messageArea.setLineWrap( true );
    messageArea.setWrapStyleWord( true );
    messageArea.setEditable( false );
    messageArea.setBorder( BorderFactory.createEmptyBorder( 5, 5, 10, 5 ) );
    messageArea.setVisible( false );
    
    JPanel editPanel = new JPanel();
    editPanel.setLayout( new BoxLayout( editPanel, BoxLayout.PAGE_AXIS ) );
    editPanel.add( messageArea );
    
    buttonPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 5, 0 ) );
    editButton = new JButton( "Edit Device" );
    editButton.setEnabled( false );
//    iconButton = new JButton( "Icon" );
    iconLabel = new JLabel( "   " );
    iconLabel.setPreferredSize( new Dimension( 100, 40 ) );
    iconLabel.setHorizontalTextPosition( SwingConstants.LEADING );
    iconLabel.setVisible( false );
    
    buttonPanel.add( editButton );
    buttonPanel.add( Box.createVerticalStrut( iconLabel.getPreferredSize().height ) );
    buttonPanel.add( iconLabel );
    editPanel.add( buttonPanel );
    editButton.addActionListener( this );
//    iconButton.addActionListener( this );
    deviceButtonPanel.add( editPanel, BorderLayout.PAGE_END );

    // deviceScrollPane.setPreferredSize( deviceButtonPanel.getPreferredSize() );

    // now the other settings table
    settingTable = new JP1Table( settingModel );
    settingTable.setCellEditorModel( settingModel );
    settingTable.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    settingTable.initColumns( settingModel );
    settingTable.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusGained( FocusEvent e )
      {
        activeTable = settingTable;
        setHighlightAction( settingTable );
      }
    } );
    settingTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
    {
      @Override
      public void valueChanged( ListSelectionEvent e )
      {
        if ( !e.getValueIsAdjusting() && !setInProgress )
        {
          setHighlightAction( settingTable );
        }
      }
    } );

    settingsScrollPane = new JScrollPane( settingTable );
    settingsScrollPane.setBorder( BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder( "Other Settings" ), settingsScrollPane.getBorder() ) );
    // settingsScrollPane.setPreferredSize( settingTable.getPreferredSize() );
    upperPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, deviceButtonPanel, settingsScrollPane );
    upperPane.setResizeWeight( 0.5 );

    notes = new JTextArea( 6, 20 );
    new TextPopupMenu( notes );
    notes.setLineWrap( true );
    notes.setWrapStyleWord( true );
    notes.getDocument().addDocumentListener( this );
    notesScrollPane = new JScrollPane( notes );
    notesScrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "General Notes" ),
        notesScrollPane.getBorder() ) );

    JPanel lowerPanel = new JPanel( new BorderLayout() );
    warningPanel = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
    warningPanel.setBackground( Color.RED );
    warningPanel.setVisible( false );

    warningLabel = new JLabel();
    Font font = warningLabel.getFont();
    Font font2 = font.deriveFont( Font.BOLD, 12 );
    warningLabel.setFont( font2 );
    warningLabel.setForeground( Color.YELLOW );

    warningPanel.add( warningLabel );
    lowerPanel.add( notesScrollPane, BorderLayout.CENTER );
    lowerPanel.add( warningPanel, BorderLayout.PAGE_END );

    mainPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, upperPane, lowerPanel );
    mainPane.setResizeWeight( 0.7 );

    add( mainPane, BorderLayout.CENTER );

    adjustPreferredViewportSizes();
  }

  private void adjustPreferredViewportSizes()
  {
    int rows = 8;
    if ( remoteConfig != null )
    {
      rows = Math.min( 8, remoteConfig.getRemote().getDeviceButtons().length );
    }
    Dimension d = deviceButtonTable.getPreferredSize();
    d.height = deviceButtonTable.getRowHeight() * rows;
    deviceButtonTable.setPreferredScrollableViewportSize( d );

    rows = 10;
    if ( remoteConfig != null )
    {
      rows = Math.min( 12, remoteConfig.getRemote().getSettings().length );
    }
    d = settingTable.getPreferredSize();
    d.height = rows * settingTable.getRowHeight();
    settingTable.setPreferredScrollableViewportSize( d );

    upperPane.resetToPreferredSizes();
    mainPane.resetToPreferredSizes();
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    setInProgress = true;
    this.remoteConfig = remoteConfig;
    deviceModel.set( remoteConfig );
    deviceButtonTable.initColumns( deviceModel );
    SoftDevices softDevices = remoteConfig.getRemote().getSoftDevices();
    Remote remote = remoteConfig.getRemote();
    
    String message1 = "Devices on this remote are selected by scrolling through a fixed list and a device with an "
      + "unset setup code is skipped.  A blank entry in the setup column denotes an unset setup code.";
    String message2 = "Devices on this remote are selected by scrolling through a list of those devices that have been "
      + "set up.  To set up an unset device, you must first set a value in the Type column.  To delete a set "
      + "device, edit the Type value and select the blank entry at the bottom of the list.";
    String message3 = "Note 1:  All devices in this remote require a corresponding device upgrade unless they consist "
      + "only of learned signals.  Use the remote itself to set up a built-in setup code, as this automatically creates "
      + "the necessary device upgrade.  You can then download it to RMIR and edit the upgrade as required.\n\nNote2:  "
      + message2;
    String text = remote.usesEZRC() ? message3 : softDevices != null && softDevices.isSetupCodesOnly() ? 
        "Note:  " + message1 : "Note:  " + message2;
    messageArea.setText( text );
    messageArea.setVisible( softDevices != null );

    if ( !remoteConfig.hasSegments() )
    {
      settingModel.set( remoteConfig );
      settingTable.initColumns( settingModel );
      settingsScrollPane.setVisible( true );
    }
    else
    {
      settingsScrollPane.setVisible( false ); 
    }
    
    iconLabel.setVisible( remote.isSSD() );
    iconLabel.setIcon( null );

    text = remoteConfig.getNotes();
    if ( text == null )
    {
      text = "";
    }
    notes.setText( text );
    notes.setCaretPosition( 0 );

    setWarning();
    adjustPreferredViewportSizes();
    validate();
    setInProgress = false;
  }

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMPanel#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    if ( listener != null )
    {
      if ( deviceModel != null )
      {
        deviceModel.addPropertyChangeListener( listener );
      }
      if ( settingModel != null )
      {
        settingModel.addPropertyChangeListener( listener );
      }
      propertyChangeSupport.addPropertyChangeListener( listener );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  @Override
  public void valueChanged( ListSelectionEvent e )
  {
    if ( !e.getValueIsAdjusting() && !setInProgress )
    {
      if ( deviceButtonTable.getSelectedRowCount() == 1 )
      {
        int selectedRow = deviceButtonTable.getSelectedRow();
        Remote remote = remoteConfig.getRemote();
        DeviceButton deviceButton = remote.getDeviceButtons()[ selectedRow ];
        selectedUpgrade = remoteConfig.getAssignedDeviceUpgrade( deviceButton );
        editButton.setEnabled( selectedUpgrade != null );
        RMIcon icon = deviceButton.icon;
        iconLabel.setIcon( icon == null ? null : icon.image );
      }
      else
      {
        editButton.setEnabled( false );
        iconLabel.setIcon( null );
      }
      deviceButtonPanel.repaint();
      setHighlightAction( deviceButtonTable );
    }
  }

  private void setHighlightAction( JP1Table table )
  {
    remoteConfig.getOwner().highlightAction.setEnabled( table.getSelectedRowCount() > 0 );
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    if ( source == editButton )
    {
      editUpgradeInRow( deviceButtonTable.getSelectedRow() );
    }
//    else if ( source == iconButton )
//    {
//      RMSetterDialog< RMIcon > dialog = new RMSetterDialog< RMIcon >();
//      RMIcon result = dialog.showDialog( this, remoteConfig, IconPanel.class, null );
//      int row = deviceButtonTable.getSelectedRow();
//      if ( row >= 0 )
//      {
//        DeviceButton db = deviceModel.getRow( row );
//        db.icon = result;
//        deviceModel.fireTableRowsUpdated( row, row );
//        iconLabel.setIcon( result.image );
//      }
//    }
  }

  public void editUpgradeInRow( int row )
  {
    if ( row == -1 )
    {
      return;
    }

    DeviceUpgrade newUpgrade = new DeviceUpgrade( selectedUpgrade );
    newUpgrade.setRemoteConfig( remoteConfig );
    List< Remote > remotes = new ArrayList< Remote >( 1 );
    remotes.add( remoteConfig.getRemote() );
    editor = new DeviceUpgradeEditor( remoteConfig.getOwner(), newUpgrade, remotes, row, this );
  }

  public void endEdit( DeviceUpgradeEditor editor, int row )
  {
    Remote remote = remoteConfig.getRemote();
    DeviceButton deviceButton = remote.getDeviceButtons()[ row ];
    DeviceUpgrade oldUpgrade = remoteConfig.getAssignedDeviceUpgrade( deviceButton );
    DeviceUpgrade newUpgrade = editor.getDeviceUpgrade();
    this.editor = null;
    if ( oldUpgrade == null || newUpgrade == null )
    {
      return;
    }

    ListIterator< DeviceUpgrade > upgrades = remoteConfig.getDeviceUpgrades().listIterator();
    while ( upgrades.hasNext() )
    {
      DeviceUpgrade upgrade = upgrades.next();
      if ( upgrade == oldUpgrade )
      {
        upgrades.set( newUpgrade );
        selectedUpgrade = newUpgrade;
        deviceModel.setValueAt( newUpgrade.getDeviceType(), row, 2 );
        deviceModel.setValueAt( new SetupCode( newUpgrade.getSetupCode() ), row, 3 );
        deviceModel.fireTableRowsUpdated( row, row );
        propertyChangeSupport.firePropertyChange( "device", null, null );
        break;
      }
    }
    remoteConfig.getOwner().getDeviceUpgradePanel().model.fireTableDataChanged();
  }

  public boolean setWarning()
  {
    boolean result = deviceModel.hasInvalidCodes() || deviceModel.hasMissingUpgrades();
    String warningText = "WARNING:";
    if ( deviceModel.hasInvalidCodes() )
    {
      warningText += "  Setup Codes shown in RED are invalid.";
    }
    if ( deviceModel.hasMissingUpgrades() )
    {
      warningText += "  Names shown in RED have no assigned upgrade.";
    }
    warningLabel.setText( warningText );
    warningPanel.setVisible( result );
    return result;
  }

  public DeviceButtonTableModel getDeviceButtonTableModel()
  {
    return deviceModel;
  }

  public JP1Table getDeviceButtonTable()
  {
    return deviceButtonTable;
  }

  public JP1Table getSettingTable()
  {
    return settingTable;
  }

  public SettingsTableModel getSettingModel()
  {
    return settingModel;
  }

  public DeviceUpgradeEditor getDeviceUpgradeEditor()
  {
    return editor;
  }

  public JP1Table getActiveTable()
  {
    return activeTable;
  }

  private RemoteConfiguration remoteConfig = null;

  private JSplitPane upperPane = null;
  private JSplitPane mainPane = null;

  private JPanel deviceButtonPanel = null;
  private JPanel warningPanel = null;
  private JLabel warningLabel = null;
  private JTextArea messageArea = null;

  private JScrollPane deviceScrollPane = null;
  private JScrollPane settingsScrollPane = null;
  private JScrollPane notesScrollPane = null;

  /** The device model. */
  private JP1Table deviceButtonTable = null;
  private DeviceButtonTableModel deviceModel = new DeviceButtonTableModel();

  /** The setting model. */
  private JP1Table settingTable = null;
  private SettingsTableModel settingModel = new SettingsTableModel();

  private JP1Table activeTable = null;

  /** The notes. */
  private JTextArea notes = null;

  private JButton editButton = null;
//  private JButton iconButton = null;
  private JPanel buttonPanel = null;
  private DeviceUpgrade selectedUpgrade = null;
  private boolean setInProgress = false;
  private JLabel iconLabel = null;
  private DeviceUpgradeEditor editor;

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  @Override
  public void changedUpdate( DocumentEvent event )
  {
    documentUpdated( event );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  @Override
  public void insertUpdate( DocumentEvent event )
  {
    documentUpdated( event );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  @Override
  public void removeUpdate( DocumentEvent event )
  {
    documentUpdated( event );
  }

  private void documentUpdated( DocumentEvent event )
  {
    if ( !setInProgress )
    {
      String text = notes.getText();
      remoteConfig.setNotes( text );
      propertyChangeSupport.firePropertyChange( "notes", null, text );
    }
  }
  
  public void finishEditing()
  {
    if ( deviceButtonTable.getCellEditor() != null )
    {
      deviceButtonTable.getCellEditor().stopCellEditing();
    }
    if ( settingTable.getCellEditor() != null )
    {
      settingTable.getCellEditor().stopCellEditing();
    }
  }
}
