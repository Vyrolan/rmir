package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.awt.datatransfer.*;

public class KeyMapMaster
 extends JFrame
 implements ActionListener, ChangeListener, DocumentListener
{
  private static KeyMapMaster me = null;
  public static final String version = "v1.08";
  private JMenuItem newItem = null;
  private JMenuItem openItem = null;
  private JMenuItem saveItem = null;
  private JMenuItem saveAsItem = null;
//  private JMenuItem importItem = null;
  private JMenuItem importFromClipboardItem = null;
  private JMenuItem exitItem = null;
  private JMenu recentFileMenu = null;
  private JMenuItem useAllRemotes = null;
  private JMenuItem usePreferredRemotes = null;
  private JMenuItem useDefaultNames = null;
  private JMenuItem useCustomNames = null;
  private JMenuItem manualItem = null;
  private JMenuItem aboutItem = null;
  private JRadioButtonMenuItem[] lookAndFeelItems = null;
  private JRadioButtonMenuItem[] promptButtons = null;
  private JLabel messageLabel = null;
  private JTextField description = null;
  private JComboBox remoteList = null;
  private JComboBox deviceTypeList = null;
  private Remote[] remotes = new Remote[ 0 ];
  private Remote[] preferredRemotes = new Remote[ 0 ];
  private Vector preferredRemoteNames = new Vector( 0 );
  private static String[] customNames = null;
  private ProtocolManager protocolManager = ProtocolManager.getProtocolManager();
  private Remote currentRemote = null;
  private String currentDeviceTypeName = null;
  private JTabbedPane tabbedPane = null;
  private SetupPanel setupPanel = null;
  private FunctionPanel functionPanel = null;
  private ExternalFunctionPanel externalFunctionPanel = null;
  private ButtonPanel buttonPanel = null;
  private LayoutPanel layoutPanel = null;
  private OutputPanel outputPanel = null;
  private KeyMapPanel keyMapPanel = null;
  private ProgressMonitor progressMonitor = null;
  private DeviceUpgrade deviceUpgrade = null;
  private static File homeDirectory = null;
  private File propertiesFile = null;
  private File rdfPath = null;
  private File upgradePath = null;
  private String lastRemoteName = null;
  private String lastRemoteSignature = null;
  private Rectangle bounds = null;
  private static String upgradeExtension = ".rmdu";
  private static String upgradeDirectory = "Upgrades";
  private int promptFlag = 0;
  private final static String[] promptStrings = { "Always", "On Exit", "Never" };
  private final static int[] promptMnemonics = { KeyEvent.VK_A, KeyEvent.VK_X, KeyEvent.VK_N };
  private final static int PROMPT_NEVER = 2;
  private final static int PROMPT_ALWAYS = 0;
  private final static int ACTION_EXIT = 1;
  private final static int ACTION_LOAD = 2;

  public KeyMapMaster( String[] args )
    throws Exception
  {
    super( "RemoteMaster" );
    me = this;

    File fileToOpen = parseArgs( args );

    setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
    setDefaultLookAndFeelDecorated( true );
    JDialog.setDefaultLookAndFeelDecorated( true );
    JFrame.setDefaultLookAndFeelDecorated( true );
    Toolkit.getDefaultToolkit().setDynamicLayout( true );

    addWindowListener( new WindowAdapter()
    {
      public void windowClosing( WindowEvent event )
      {
        try
        {
          if ( !promptToSaveUpgrade( ACTION_EXIT ))
            return;
          savePreferences();
        }
        catch ( Exception e )
        {
          System.err.println( "KeyMapMaster.windowClosing() caught an exception!" );
          e.printStackTrace( System.out );
        }
        System.exit( 0 );
      }
    });

    createMenus();

    loadPreferences();

    deviceUpgrade = new DeviceUpgrade();

    Container mainPanel = getContentPane();
    tabbedPane = new JTabbedPane();
    mainPanel.add( tabbedPane, BorderLayout.CENTER );

    double b = 10;       // space around border/columns
    double i = 5;        // space between rows
    double f = TableLayout.FILL;
    double p = TableLayout.PREFERRED;
    double size[][] =
    {
      { b, p, b, f, b, p, b, p, b },                     // cols
      { b, p, i, p, b }         // rows
    };
    TableLayout tl = new TableLayout( size );
    JPanel panel = new JPanel( tl );

    JLabel label = new JLabel( "Description:" );
    panel.add( label, "1, 1" );
    description = new JTextField( 50 );
    label.setLabelFor( description );
    description.getDocument().addDocumentListener( this );
    panel.add( description, "3, 1, 7, 1" );

    new TextPopupMenu( description );

    label = new JLabel( "Remote:" );
    panel.add( label, "1, 3" );
    remoteList = new JComboBox();
    label.setLabelFor( remoteList );
    remoteList.setMaximumRowCount( 16 );
    remoteList.setPrototypeDisplayValue( "A Really Long Remote Control Name with an Extender and more" );
    remoteList.setToolTipText( "Choose the remote for the upgrade being created." );
    panel.add( remoteList, "3, 3" );

    label = new JLabel( "Device Type:" );
    panel.add( label, "5, 3" );
//    String[] aliasNames = deviceUpgrade.getDeviceTypeAliasNames();
    deviceTypeList = new JComboBox();
    label.setLabelFor( deviceTypeList );
    deviceTypeList.setPrototypeDisplayValue( "A Device Type" );
    deviceTypeList.setToolTipText( "Choose the device type for the upgrade being created." );
    panel.add( deviceTypeList, "7, 3" );

    mainPanel.add( panel, BorderLayout.NORTH );

    messageLabel = new JLabel( " " );
    messageLabel.setForeground( Color.red );

    mainPanel.add( messageLabel, BorderLayout.SOUTH );

    protocolManager.load( new File( homeDirectory, "protocols.ini" ));

    setupPanel = new SetupPanel( deviceUpgrade );
    setupPanel.setToolTipText( "Enter general information about the upgrade." );
    currPanel = setupPanel;
    addPanel( setupPanel ); 

    functionPanel = new FunctionPanel( deviceUpgrade );
    functionPanel.setToolTipText( "Define function names and parameters." );
    addPanel( functionPanel );

    externalFunctionPanel = new ExternalFunctionPanel( deviceUpgrade );
    externalFunctionPanel.setToolTipText( "Define functions from other device codes." );
    addPanel( externalFunctionPanel ); 

    buttonPanel = new ButtonPanel( deviceUpgrade );
    buttonPanel.setToolTipText( "Assign functions to buttons." );
    addPanel( buttonPanel ); 

    layoutPanel = new LayoutPanel( deviceUpgrade );
    layoutPanel.setToolTipText( "Button Layout information." );
    addPanel( layoutPanel ); 

    keyMapPanel = new KeyMapPanel( deviceUpgrade );
    keyMapPanel.setToolTipText( "Printable list of buttons and their assigned functions" );
    addPanel( keyMapPanel ); 

    outputPanel = new OutputPanel( deviceUpgrade );
    outputPanel.setToolTipText( "The output to copy-n-paste into IR." );
    addPanel( outputPanel ); 

    RemoteManager rm = RemoteManager.getRemoteManager();
    rdfPath = rm.loadRemotes( rdfPath );

    Vector work = new Vector();
    for ( Enumeration e = preferredRemoteNames.elements(); e.hasMoreElements(); )
    {
      String name = ( String )e.nextElement();
      Remote temp = rm.findRemoteByName( name );
      if ( temp != null )
        work.add( temp );
    }
    preferredRemotes = ( Remote[] )work.toArray( preferredRemotes );

    if ( preferredRemotes.length == 0 )
    {
      useAllRemotes.setSelected( true );
      usePreferredRemotes.setEnabled( false );
    }

    setRemotes();

    Remote temp = null;
    if ( lastRemoteName != null )
      temp = rm.findRemoteByName( lastRemoteName );
    if ( temp == null )
      temp = rm.getRemotes()[ 0 ];
    temp.load();
    Protocol protocol =
      ( Protocol )protocolManager.getProtocolsForRemote( temp ).elementAt( 0 );
    deviceUpgrade.setProtocol( protocol );
    setRemote( temp );
    remoteList.setSelectedItem( temp );

    remoteList.addActionListener( this );
    deviceTypeList.addActionListener( this );
    tabbedPane.addChangeListener( this );

    currPanel.update();

    clearMessage();

    if ( bounds != null )
      setBounds( bounds );
    else
      pack();

    loadUpgrade( fileToOpen );

    show();
  }

  public static KeyMapMaster getKeyMapMaster(){ return me;}

  private void createMenus()
  {
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar( menuBar );
    JMenu menu = new JMenu( "File" );
    menu.setMnemonic( KeyEvent.VK_F );
    menuBar.add( menu );
    newItem = new JMenuItem( "New" );
    newItem.setMnemonic( KeyEvent.VK_N );
    newItem.addActionListener( this );
    menu.add( newItem );
    openItem = new JMenuItem( "Open..." );
    openItem.setMnemonic( KeyEvent.VK_O );
    openItem.addActionListener( this );
    menu.add( openItem );
    saveItem = new JMenuItem( "Save" );
    saveItem.setMnemonic( KeyEvent.VK_S );
    saveItem.setEnabled( false );
    saveItem.addActionListener( this );
    menu.add( saveItem );
    saveAsItem = new JMenuItem( "Save as..." );
    saveAsItem.setMnemonic( KeyEvent.VK_A );
    saveAsItem.setDisplayedMnemonicIndex( 5 );
    saveAsItem.addActionListener( this );
    menu.add( saveAsItem );

    menu.addSeparator();

//    importItem = new JMenuItem( "Import KM file..." );
//    importItem.setMnemonic( KeyEvent.VK_K );
//    importItem.addActionListener( this );
//    menu.add( importItem );
//
    importFromClipboardItem = new JMenuItem( "Import from Clipboard" );
    importFromClipboardItem.setMnemonic( KeyEvent.VK_C );
    importFromClipboardItem.addActionListener( this );
    menu.add( importFromClipboardItem );

    menu.addSeparator();

    recentFileMenu = new JMenu( "Recent" );
    recentFileMenu.setMnemonic( KeyEvent.VK_R );
    recentFileMenu.setEnabled( false );
    menu.add( recentFileMenu );
    menu.addSeparator();

    exitItem = new JMenuItem( "Exit" );
    exitItem.setMnemonic( KeyEvent.VK_X );
    exitItem.addActionListener( this );
    menu.add( exitItem );

    menu = new JMenu( "Options" );
    menu.setMnemonic( KeyEvent.VK_O );
    menuBar.add( menu );

    JMenu submenu = new JMenu( "Look and Feel" );
    submenu.setMnemonic( KeyEvent.VK_L );
    menu.add( submenu );

    ButtonGroup group = new ButtonGroup();
    UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();

    ActionListener al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        try
        {
          UIManager.setLookAndFeel((( JRadioButtonMenuItem )e.getSource()).getActionCommand());
          SwingUtilities.updateComponentTreeUI( me );
        }
        catch ( Exception x )
        {}
      }
    };

    lookAndFeelItems = new JRadioButtonMenuItem[ info.length ];
    for ( int i = 0; i < info.length; i++ )
    {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem( info[ i ].getName());
      lookAndFeelItems[ i ] = item;
      item.setMnemonic( item.getText().charAt( 0 ));
      item.setActionCommand( info[ i ].getClassName());
      group.add( item );
      submenu.add( item );
      item.addActionListener( al );
    }

    group = new ButtonGroup();
    submenu = new JMenu( "Prompt to Save" );
    submenu.setMnemonic( KeyEvent.VK_P );
    menu.add( submenu );

    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        Object source = e.getSource();
        for ( int i = 0; i < promptButtons.length; i++ )
          if ( promptButtons[ i ] == source )
          {
            promptFlag = i;
            break;
          }
      }
    };

    promptButtons = new JRadioButtonMenuItem[ promptStrings.length ];
    for ( int i = 0; i < promptStrings.length; i++ )
    {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem( promptStrings[ i ] );
      item.setMnemonic( promptMnemonics[ i ]);
      promptButtons[ i ] = item;
      item.addActionListener( al );
      group.add( item );
      submenu.add( item );
    }
    promptButtons[ promptFlag ].setSelected( true );

    submenu = new JMenu( "Remotes" );
    submenu.setMnemonic( KeyEvent.VK_R );
    menu.add( submenu );

    group = new ButtonGroup();
    useAllRemotes = new JRadioButtonMenuItem( "All" );
    useAllRemotes.setMnemonic( KeyEvent.VK_A );
    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        Object source = e.getSource();
        if (( source == useAllRemotes ) || ( source == usePreferredRemotes ))
          setRemotes();
        else
          editPreferredRemotes();
      }
    };
    useAllRemotes.setSelected( true );
    useAllRemotes.addActionListener( al );
    group.add( useAllRemotes );
    submenu.add( useAllRemotes );

    usePreferredRemotes = new JRadioButtonMenuItem( "Preferred" );
    usePreferredRemotes.setMnemonic( KeyEvent.VK_P );
    usePreferredRemotes.addActionListener( al );
    group.add( usePreferredRemotes );
    submenu.add( usePreferredRemotes );

    submenu.addSeparator();
    JMenuItem item = new JMenuItem( "Edit preferred..." );
    item.setMnemonic( KeyEvent.VK_E );
    item.addActionListener( al );
    submenu.add( item );

    submenu = new JMenu( "Function names" );
    submenu.setMnemonic( KeyEvent.VK_F );
    menu.add( submenu );

    group = new ButtonGroup();
    useDefaultNames = new JRadioButtonMenuItem( "Default" );
    useDefaultNames.setMnemonic( KeyEvent.VK_D );
    al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        Object source = e.getSource();
        if (( source != useDefaultNames ) && ( source != useCustomNames ))
          editCustomNames();
      }
    };
    useDefaultNames.setSelected( true );
    useDefaultNames.addActionListener( al );
    group.add( useDefaultNames );
    submenu.add( useDefaultNames );

    useCustomNames = new JRadioButtonMenuItem( "Custom" );
    useCustomNames.setMnemonic( KeyEvent.VK_C );
    useCustomNames.addActionListener( al );
    group.add( useCustomNames );
    submenu.add( useCustomNames );

    submenu.addSeparator();
    item = new JMenuItem( "Edit custom names..." );
    item.setMnemonic( KeyEvent.VK_E );
    item.addActionListener( al );
    submenu.add( item );

    menu = new JMenu( "Advanced" );
    menu.setMnemonic( KeyEvent.VK_A );
    menuBar.add( menu );

    manualItem = new JMenuItem( "Manual settings..." );
    manualItem.setMnemonic( KeyEvent.VK_M );
    manualItem.addActionListener( this );
    menu.add( manualItem );

    menu = new JMenu( "Help" );
    menu.setMnemonic( KeyEvent.VK_H );
    menuBar.add( menu );

    aboutItem = new JMenuItem( "About..." );
    aboutItem.setMnemonic( KeyEvent.VK_A );
    aboutItem.addActionListener( this );
    menu.add( aboutItem );
  }

  public void addPanel( KMPanel panel )
  {
    tabbedPane.addTab( panel.getName(), null, panel, panel.getToolTipText());
  }

  public void addPanel( KMPanel panel, int index )
  {
    System.err.println( "KeyMapMaster.addPanel()" + panel );
    tabbedPane.insertTab( panel.getName(), null, panel, panel.getToolTipText(), index );
  }

  public void removePanel( KMPanel panel )
  {
    System.err.println( "KeyMapMaster.removePanel()" + panel );
    tabbedPane.removeTabAt( 1 );
//    tabbedPane.remove( panel );
    tabbedPane.validate();
  }

  private void editPreferredRemotes()
  {
    PreferredRemoteDialog d = new PreferredRemoteDialog( this, preferredRemotes );
    d.show();
    if ( d.getUserAction() == JOptionPane.OK_OPTION )
    {
      preferredRemotes = d.getPreferredRemotes();
      if ( preferredRemotes.length == 0 )
      {
        usePreferredRemotes.setEnabled( false );
        if  ( !useAllRemotes.isSelected())
        {
          useAllRemotes.setSelected( true );
          Remote r = ( Remote )remoteList.getSelectedItem();
          remoteList.removeActionListener( this );
          remoteList.setModel( new DefaultComboBoxModel( remotes ));
          remoteList.setSelectedItem( r );
          remoteList.addActionListener( this );
        }
      }
      else
        usePreferredRemotes.setEnabled( true );

      if ( usePreferredRemotes.isSelected())
        remoteList.setModel( new DefaultComboBoxModel( preferredRemotes ));
    }
  }

  private void editCustomNames()
  {
    CustomNameDialog d = new CustomNameDialog( this, customNames, deviceUpgrade );
    d.show();
    if ( d.getUserAction() == JOptionPane.OK_OPTION )
    {
      customNames = d.getCustomNames();
      if (( customNames == null ) || customNames.length == 0 )
      {
        useCustomNames.setEnabled( false );
        useDefaultNames.setSelected( true );
      }
      else
        useCustomNames.setEnabled( true );
    }
  }

  private void editManualSettings()
  {
    System.err.println( "editManualSettings()");
    ManualSettingsDialog d = 
      new ManualSettingsDialog( this, 
                               ( ManualProtocol )protocolManager.findByName( "Manual Settings" ).firstElement());
    d.show();
    if ( d.getUserAction() == JOptionPane.OK_OPTION )
    {
      
    }
  }

  private File parseArgs( String[] args )
  {
    homeDirectory = new File( System.getProperty( "user.dir" ));
    File fileToOpen = null;
    for ( int i = 0; i < args.length; i++ )
    {
      String arg = args[ i ];
      if ( arg.charAt( 0 ) == '-' )
      {
        char flag = arg.charAt( 1 );
        String parm = args[ ++i ];
        if ( flag == 'h' )
        {
          homeDirectory = new File( parm );
        }
        else if ( flag == 'p' )
        {
          propertiesFile = new File( parm );
        }
      }
      else
        fileToOpen = new File( arg );
    }
    try
    {
      System.setErr( new PrintStream( new FileOutputStream( new File ( homeDirectory, "rmaster.err" ))));
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.err );
    }
    if ( propertiesFile == null )
    {
      propertiesFile = new File( homeDirectory, "RemoteMaster.properties" );
    }

    return fileToOpen;
  }

  public static void showMessage( String msg )
  {
    if ( msg.length() == 0 )
      msg = " ";
    me.messageLabel.setText( msg );
    Toolkit.getDefaultToolkit().beep();
  }

  public static void clearMessage()
  {
    me.messageLabel.setText( " " );
  }

  public void setRemotes()
  {
    if ( remoteList != null )
    {
      if ( usePreferredRemotes.isSelected())
        remoteList.setModel( new DefaultComboBoxModel( preferredRemotes ));
      else
        remoteList.setModel( new DefaultComboBoxModel( RemoteManager.getRemoteManager().getRemotes()));
    }
  }

  public void setRemote( Remote remote )
  {
    if (( remoteList != null ) && ( remote != currentRemote ))
    {
      try
      {
        remote.load();
        String[] aliasNames = remote.getDeviceTypeAliasNames();
        String alias = deviceUpgrade.getDeviceTypeAliasName();
        deviceTypeList.removeActionListener( this );
        deviceTypeList.setModel( new DefaultComboBoxModel( aliasNames ));
        deviceTypeList.setMaximumRowCount( aliasNames.length );

        int index = 0;
        for ( index = 0; index < aliasNames.length; index++ )
        {
          if ( aliasNames[ index ].equals( alias ))
            break;
        }
        while (( index == aliasNames.length ))
        {
          String msg = "Remote \"" + remote.getName() + "\" does not support the device type " +
          alias + ".  Please select one of the supported device types below to use instead.\n";
          String rc = ( String )JOptionPane.showInputDialog( null,
                                                             msg,
                                                             "Unsupported Device Type",
                                                             JOptionPane.ERROR_MESSAGE,
                                                             null,
                                                             aliasNames,
                                                             null );
          for ( index = 0; index < aliasNames.length; index++ )
          {
            if ( aliasNames[ index ].equals( rc ))
              break;
          }
        }

        deviceTypeList.setSelectedIndex( index );

        currentRemote = remote;
        deviceUpgrade.setRemote( remote );
        deviceUpgrade.setDeviceTypeAliasName( aliasNames[ index ]);
        deviceTypeList.addActionListener( this );
      }
      catch ( Exception e )
      {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        e.printStackTrace( pw );
        pw.flush();
        pw.close();
        JOptionPane.showMessageDialog( null, sw.toString(), "Remote Load Error",
                                       JOptionPane.ERROR_MESSAGE );
        System.err.println( sw.toString());
      }
    }
  }

  public void setDeviceTypeName( String aliasName )
  {
    if (( deviceTypeList != null ) && ( aliasName != currentDeviceTypeName ))
    {
      currentDeviceTypeName = aliasName;
      deviceUpgrade.setDeviceTypeAliasName( aliasName );
      deviceTypeList.setSelectedItem( aliasName );
    }
  }

  // ActionListener Methods
  public void actionPerformed( ActionEvent e )
  {
    try
    {
      Object source = e.getSource();

      if ( source == remoteList )
      {
        Remote remote = ( Remote )remoteList.getSelectedItem();
        setRemote( remote );
        validateUpgrade();
        currPanel.update();
      }
      else if ( source == deviceTypeList )
      {
        String typeName = ( String )deviceTypeList.getSelectedItem();
        setDeviceTypeName( typeName );
        currPanel.update();
      }
      else if ( source == newItem )
      {
        if ( !promptToSaveUpgrade( ACTION_LOAD ))
          return;
        Protocol oldProtocol = deviceUpgrade.getProtocol();
        deviceUpgrade.reset();
        Protocol newProtocol = deviceUpgrade.getProtocol();
        if ( newProtocol != oldProtocol )
        {
          KMPanel oldPanel = oldProtocol.getPanel( deviceUpgrade );
          if ( oldPanel != null )
            removePanel( oldPanel );
          KMPanel newPanel = newProtocol.getPanel( deviceUpgrade );
          if ( newPanel != null )
            addPanel( newPanel, 1 );
          if (( oldPanel != null ) || ( newPanel != null ))
            tabbedPane.validate();
        }
        oldProtocol.reset();
        setTitle( "RemoteMapMaster" );
        description.setText( null );
        remoteList.setSelectedItem( deviceUpgrade.getRemote());
        deviceTypeList.setSelectedItem( deviceUpgrade.getDeviceTypeAliasName());
        saveItem.setEnabled( false );
        currPanel.update();
      }
      else if ( source == saveItem )
      {
        currPanel.commit();
        deviceUpgrade.store();
      }
      else if ( source == saveAsItem )
      {
        currPanel.commit();
        saveAs();
      }
      else if ( source == openItem )
      {
        if ( !promptToSaveUpgrade( ACTION_LOAD ))
          return;
        File file = getUpgradeFile( upgradePath );
        loadUpgrade( file );
      }
      else if ( source == importFromClipboardItem )
      {
        if ( !promptToSaveUpgrade( ACTION_LOAD ))
          return;
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable clipData = clipboard.getContents( clipboard );
        if ( clipData != null )
        {
          try
          {
            if ( clipData.isDataFlavorSupported( DataFlavor.stringFlavor ))
            {
              String s =
                ( String )( clipData.getTransferData( DataFlavor.stringFlavor ));
              BufferedReader in = new BufferedReader( new StringReader( s ));
              loadUpgrade( in );
            }
          }
          catch (Exception ex)
          {
            ex.printStackTrace( System.err );
          }
        }
      }
      else if ( source == exitItem )
      {
        dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ));
      }
      else if ( source == manualItem )
      {
        editManualSettings();
      }
      else if ( source == aboutItem )
      {
        String text = "RemoteMaster Device Upgrade Editor, " + version + "\n" +
                      "Get the latest version at http://controlremote.sourceforge.net\n\n" +
                      "Java version " + System.getProperty( "java.version" ) + " from " + System.getProperty( "java.vendor" ) + "\n\n" +
                      "RDFs loaded from \"" + rdfPath + "\"\n\n" +
                      "Written primarily by Greg Bush\n\n" +
                      "Other contributors include:\n" +
                      "John S Fine, Nils Ekberg, Jon Armstrong, Robert Crowe,\n" +
                      "Mark Pauker, Mark Pierson, Mike England, and more\n\n";

        JOptionPane.showMessageDialog( this, text, "About RemoteMaster", JOptionPane.INFORMATION_MESSAGE );
      }
    }
    catch ( Exception ex )
    {
      ex.printStackTrace( System.err );
    }
  } // actionPerformed

  private void removeListeners()
  {
    description.getDocument().removeDocumentListener( this );
    remoteList.removeActionListener( this );
    deviceTypeList.removeActionListener( this );
  }

  private void addListeners()
  {
    description.getDocument().addDocumentListener( this );
    remoteList.addActionListener( this );
    deviceTypeList.addActionListener( this );
  }

  public static File promptForUpgradeFile( File path )
  {
    return me.getUpgradeFile( path );
  }

  public File getUpgradeFile( File path )
  {
    if ( path == null )
      path = upgradePath;

    File file = null;
    JFileChooser chooser = new JFileChooser( upgradePath );
    try 
    {
      chooser.setAcceptAllFileFilterUsed( false );
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.err );
    }
    chooser.setFileFilter( new AnyFileFilter());
    chooser.addChoosableFileFilter( new TextFileFilter());
    chooser.addChoosableFileFilter( new KMFileFilter());
    int returnVal = chooser.showOpenDialog( this );
    if ( returnVal == JFileChooser.APPROVE_OPTION )
    {
      file = chooser.getSelectedFile();

      int rc = JOptionPane.YES_OPTION;
      if ( !file.exists())
      {
        JOptionPane.showMessageDialog( this,
                                       file.getName() + " doesn't exist.",
                                       "File doesn't exist.",
                                       JOptionPane.ERROR_MESSAGE );
      }
      else if ( file.isDirectory())
      {
        JOptionPane.showMessageDialog( this,
                                       file.getName() + " is a directory.",
                                       "File doesn't exist.",
                                       JOptionPane.ERROR_MESSAGE );
      }
      else
      {
        String str = file.getName().toLowerCase();
        upgradePath = file.getParentFile();
      }
    }
    return file;
  }

  public void saveAs()
    throws IOException
  {
    JFileChooser chooser = new JFileChooser( upgradePath );
    chooser.setFileFilter( new KMFileFilter());
    File f = deviceUpgrade.getFile();
    if ( f == null )
      f = new File( upgradePath, deviceUpgrade.getDescription() + upgradeExtension );
    chooser.setSelectedFile( f );
    int returnVal = chooser.showSaveDialog( this );
    if ( returnVal == JFileChooser.APPROVE_OPTION )
    {
      String name = chooser.getSelectedFile().getAbsolutePath();
      if ( !name.toLowerCase().endsWith( upgradeExtension ))
        name = name + upgradeExtension;
      File file = new File( name );
      int rc = JOptionPane.YES_OPTION;
      if ( file.exists())
      {
        rc = JOptionPane.showConfirmDialog( this,
                                            file.getName() + " already exists.  Do you want to repalce it?",
                                            "Replace existing file?",
                                            JOptionPane.YES_NO_OPTION );
      }
      if ( rc == JOptionPane.YES_OPTION )
      {
        deviceUpgrade.store( file );
        saveItem.setEnabled( true );
        setTitle( file.getCanonicalPath() + " - RemoteMaster" );
      }
    }
  }

  public boolean promptToSaveUpgrade( int action )
    throws IOException
  {
    if ( promptFlag == PROMPT_NEVER )
      return true;
    else if ( promptFlag != PROMPT_ALWAYS )
    {
      if ( action != ACTION_EXIT )
        return true;
    }

    int rc = JOptionPane.showConfirmDialog( this,
//                                            "All changes made to the current upgrade will be lost if you proceed.\n\n" +
                                            "Do you want to save the current upgrade before proceeding?",
                                            "Save upgrade?",
                                            JOptionPane.YES_NO_CANCEL_OPTION );
    if (( rc == JOptionPane.CANCEL_OPTION ) || ( rc == JOptionPane.CLOSED_OPTION ))
      return false;
    if ( rc == JOptionPane.NO_OPTION )
      return true;

    currPanel.commit();
    if ( deviceUpgrade.getFile() != null )
      deviceUpgrade.store();
    else
      saveAs();
    return true;
  }

  public void loadUpgrade( File file )
    throws Exception
  {
    if ( file == null )
      return;

    Protocol oldProtocol = deviceUpgrade.getProtocol();
    KMPanel oldPanel = oldProtocol.getPanel( deviceUpgrade );
    if ( oldPanel != null )
      removePanel( oldPanel );
    deviceUpgrade.reset();
    deviceUpgrade.load( file );
    Protocol newProtocol = deviceUpgrade.getProtocol();
    if ( newProtocol == null )
      return;
    KMPanel newPanel = newProtocol.getPanel( deviceUpgrade );
    if ( newPanel != null )
      addPanel( newPanel, 1 );
    if (( oldPanel != null ) || ( newPanel != null ))
      tabbedPane.validate();
    refresh();

    boolean isRMDU = file.getName().toLowerCase().endsWith( ".rmdu" );

    if ( isRMDU )
    {
      setTitle( file.getCanonicalPath() + " - RemoteMaster" );

      int i = recentFileMenu.getItemCount() - 1;
      while ( i >= 0 )
      {
        JMenuItem item = recentFileMenu.getItem( i );
        FileAction action = ( FileAction  )item.getAction();
        File f = action.getFile();
        if ( f.getAbsolutePath().equals( file.getAbsolutePath()))
          recentFileMenu.remove( i );
        --i;
      }
      i = recentFileMenu.getItemCount();
      while ( i > 9 )
        recentFileMenu.remove( --i );
      recentFileMenu.add( new JMenuItem( new FileAction( file )), 0 );

      recentFileMenu.setEnabled( true );
    }
    else
      setTitle( "RemoteMaster" );
    upgradePath = file.getParentFile();
  }

  public void loadUpgrade( BufferedReader reader )
    throws Exception
  {
    Protocol oldProtocol = deviceUpgrade.getProtocol();
    KMPanel oldPanel = oldProtocol.getPanel( deviceUpgrade );
    if ( oldPanel != null )
      removePanel( oldPanel );
    deviceUpgrade.reset();
    deviceUpgrade.load( reader );
    Protocol newProtocol = deviceUpgrade.getProtocol();
    KMPanel newPanel = newProtocol.getPanel( deviceUpgrade );
    if ( newPanel != null )
      addPanel( newPanel, 1 );
    if (( oldPanel != null ) || ( newPanel != null ))
      tabbedPane.validate();
    refresh();
  }

  private void refresh()
  {
    String title = "RemoteMaster";
    File file = deviceUpgrade.getFile();
    if ( file != null )
    try 
    {
      title = file.getCanonicalPath() + " - RemoteMaster";
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.err );
    }

    saveItem.setEnabled( file != null );
    removeListeners();   
    description.setText( deviceUpgrade.getDescription());
    String savedTypeName = deviceUpgrade.getDeviceTypeAliasName();
    Remote r = deviceUpgrade.getRemote();
    setRemote( r );
    remoteList.setSelectedItem( r );
    if ( remoteList.getSelectedItem() != r )
    {
      remoteList.addItem( r );
      remoteList.setSelectedItem( r );
    }
    setDeviceTypeName( savedTypeName );
    addListeners();
    currPanel.update();

    validateUpgrade();
  }

  public void importUpgrade( BufferedReader in )
    throws Exception
  {
    deviceUpgrade.reset();
    deviceUpgrade.importUpgrade( in );
    setTitle( "RemoteMaster" );
    removeListeners();
    description.setText( deviceUpgrade.getDescription());
    String savedTypeName = deviceUpgrade.getDeviceTypeAliasName();
    Remote r = deviceUpgrade.getRemote();
    setRemote( r );
    remoteList.setSelectedItem( r );
    if ( remoteList.getSelectedItem() != r )
    {
      remoteList.addItem( r );
      remoteList.setSelectedItem( r );
    }
    setDeviceTypeName( savedTypeName );
    addListeners();
    currPanel.update();

    validateUpgrade();
  }

  // ChangeListener methods
  private KMPanel currPanel = null;
  public void stateChanged( ChangeEvent e )
  {
    currPanel.commit();
    currPanel = ( KMPanel )(( JTabbedPane )e.getSource()).getSelectedComponent();
    currPanel.update();
    SwingUtilities.updateComponentTreeUI( currPanel );
    validateUpgrade();
  }

  public static File getHomeDirectory()
  {
    return homeDirectory;
  }

  public static String[] getCustomNames()
  {
    if ( me.useCustomNames.isSelected())
      return customNames;
    else
      return null;
  }

  private void loadPreferences()
    throws Exception
  {
    Properties props = new Properties();

    if ( propertiesFile.canRead())
    {
      FileInputStream in = new FileInputStream( propertiesFile );
      props.load( in );
      in.close();
    }

    String temp = props.getProperty( "RDFPath" );
    if ( temp != null )
      rdfPath = new File( temp );
    else
      rdfPath = new File( homeDirectory, "rdf" );
    while ( !rdfPath.exists() && !rdfPath.isDirectory())
      rdfPath = rdfPath.getParentFile();

    temp = props.getProperty( "UpgradePath" );
    if ( temp == null )
      temp = props.getProperty( "KMPath" );
    if ( temp != null )
      upgradePath = new File( temp );
    else
      upgradePath = new File( homeDirectory, upgradeDirectory );
    while ( !upgradePath.exists() && !upgradePath.isDirectory())
      upgradePath = upgradePath.getParentFile();

    String defaultLookAndFeel = UIManager.getSystemLookAndFeelClassName();
    temp = props.getProperty( "LookAndFeel", defaultLookAndFeel );
    try
    {
      UIManager.setLookAndFeel( temp );
      SwingUtilities.updateComponentTreeUI( this );
      for ( int i = 0; i < lookAndFeelItems.length; i ++ )
      {
        if ( lookAndFeelItems[ i ].getActionCommand().equals( temp ))
        {
          lookAndFeelItems[ i ].setSelected( true );
          break;
        }
      }
    }
    catch ( Exception e )
    {
      System.err.println( "Exception thrown when setting look and feel to " + temp );
    }

    lastRemoteName = props.getProperty( "Remote.name" );
    lastRemoteSignature = props.getProperty( "Remote.signature" );

    temp = props.getProperty( "PromptToSave", promptStrings[ 0 ] );
    for ( int i = 0; i < promptStrings.length; i++ )
      if ( promptStrings[ i ].equals( temp ))
        promptFlag = i;
    if ( promptFlag > promptStrings.length )
      promptFlag = 0;

    promptButtons[ promptFlag ].setSelected( true );

    for ( int i = 0; true; i++ )
    {
      temp = props.getProperty( "PreferredRemotes." + i );
      if ( temp == null )
        break;
      System.err.println( "Preferred remote name " + temp );
      preferredRemoteNames.add( temp );
    }

    temp = props.getProperty( "ShowRemotes", "All" );
    if ( temp.equals( "All" ))
      useAllRemotes.setSelected( true );
    else
      usePreferredRemotes.setSelected( true );

    for ( int i = 0; i < 10; i++ )
    {
      temp = props.getProperty( "RecentFiles." + i );
      if ( temp == null )
        break;
      recentFileMenu.add( new FileAction( new File( temp )));
    }
    if ( recentFileMenu.getItemCount() > 0 )
      recentFileMenu.setEnabled( true );

    temp = props.getProperty( "Bounds" );
    if ( temp != null )
    {
      bounds = new Rectangle();
      StringTokenizer st = new StringTokenizer( temp, "," );
      bounds.x = Integer.parseInt( st.nextToken());
      bounds.y = Integer.parseInt( st.nextToken());
      bounds.width = Integer.parseInt( st.nextToken());
      bounds.height = Integer.parseInt( st.nextToken());
    }

    temp = props.getProperty( "CustomNames" );
    if ( temp != null )
    {
      StringTokenizer st = new StringTokenizer( temp, "|" );
      int count = st.countTokens();
      customNames = new String[ count ];
      for ( int i = 0; i < count; i++ )
        customNames[ i ] = st.nextToken();
    }

    temp = props.getProperty( "UseCustomNames" );
    useCustomNames.setSelected( temp != null );
  }

  private void savePreferences()
    throws Exception
  {
    Properties props = new Properties();
    props.setProperty( "RDFPath", rdfPath.getAbsolutePath());
    props.setProperty( "UpgradePath", upgradePath.getAbsolutePath());
    props.setProperty( "LookAndFeel", UIManager.getLookAndFeel().getClass().getName());
    Remote remote = deviceUpgrade.getRemote();
    props.setProperty( "Remote.name", remote.getName());
    props.setProperty( "Remote.signature", remote.getSignature());
    props.setProperty( "PromptToSave", promptStrings[ promptFlag ]);
    if ( useAllRemotes.isSelected())
      props.setProperty( "ShowRemotes", "All" );
    else
      props.setProperty( "ShowRemotes", "Preferred" );

    for ( int i = 0; i < recentFileMenu.getItemCount(); i++ )
    {
      JMenuItem item = recentFileMenu.getItem( i );
      FileAction action = ( FileAction )item.getAction();
      File f = action.getFile();
      props.setProperty( "RecentFiles." + i, f.getAbsolutePath());
    }

    for ( int i = 0; i < preferredRemotes.length; i++ )
    {
      Remote r = preferredRemotes[ i ];
      props.setProperty( "PreferredRemotes." + i, r.getName());
    }

    int state = getExtendedState();
    if ( state != Frame.NORMAL )
      setExtendedState( Frame.NORMAL );
    Rectangle bounds = getBounds();
    props.setProperty( "Bounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );

    if ( useCustomNames.isSelected())
      props.setProperty( "UseCustomNames", "yes" );

    if ( customNames != null )
    {
      StringBuffer value = new StringBuffer();
      for ( int i = 0; i < customNames.length; i++ )
      {
        if ( i != 0 )
          value.append( '|' );
        value.append( customNames[ i ]);
      }
      props.setProperty( "CustomNames", value.toString() );
    }
    
    FileOutputStream out = new FileOutputStream( propertiesFile );
    props.store( out, null );
    out.flush();
    out.close();
  }

  public void validateUpgrade()
  {
    Remote r = deviceUpgrade.getRemote();
    Protocol p = deviceUpgrade.getProtocol();
    Vector protocols = protocolManager.getProtocolsForRemote( r );
    if ( !protocols.contains( p ) && !p.hasCode( r ))
    {
      System.err.println( "KeyMapMaster.validateUpgrade(), protocol " + p.getDiagnosticName() +
                          "is not compatible with remote " + r.getName());

      // Find a matching protocol for this remote
      Protocol match = null;
      String name = p.getName();
      for ( Enumeration e = protocols.elements(); e.hasMoreElements(); )
      {
        Protocol p2 = ( Protocol )e.nextElement();
        if ( p2.getName().equals( name ))
        {
          match = p2;
          System.err.println( "\tFound one with the same name: " + p2.getDiagnosticName());
          break;
        }
      }

      if ( match != null )
      {
        System.err.println( "\tChecking for matching dev. parms" );
        DeviceParameter[] parms = p.getDeviceParameters();
        DeviceParameter[] parms2 = match.getDeviceParameters();

        int[] map = new int[ parms.length ];
        boolean parmsMatch = true;
        for ( int i = 0; i < parms.length; i++ )
        {
          name = parms[ i ].getName();
          System.err.print( "\tchecking " + name );
          boolean nameMatch = false;
          for ( int j = 0; j < parms2.length; j++ )
          {
            if ( name.equals( parms2[ j ].getName()))
            {
              map[ i ] = j;
              nameMatch = true;
              System.err.print( " has a match!" );
              break;
            }
          }
          System.err.println();
          parmsMatch = nameMatch;
          if ( !parmsMatch )
            break;
        }
        if ( parmsMatch )
        {
          // copy parameters from p to p2!
          System.err.println( "\tCopying dev. parms" );
          for ( int i = 0; i < map.length; i++ )
          {
            System.err.println( "\tfrom index " + i + " to index " + map[ i ]);
            parms2[ map[ i ]].setValue( parms[ i ].getValue());
          }
          System.err.println();
          System.err.println( "Setting new protocol" );
          p.convertFunctions( deviceUpgrade.getFunctions(), match );
          deviceUpgrade.setProtocol( match );
          return;
        }
      }
      JOptionPane.showMessageDialog( this,
                                     "The selected protocol " + p.getDiagnosticName() +
                                     "\nis not compatible with the selected remote.\n" +
                                     "This upgrade will NOT function correctly.\n" +
                                     "Please choose a different protocol.",
                                     "Error", JOptionPane.ERROR_MESSAGE );

    }
  }

  private void updateDescription()
  {
    deviceUpgrade.setDescription( description.getText());
    currPanel.update();
  }

  // DocumentListener methods
  public void changedUpdate( DocumentEvent e )
  {
    updateDescription();
  }

  public void insertUpdate( DocumentEvent e )
  {
    updateDescription();
  }

  public void removeUpdate( DocumentEvent e )
  {
    updateDescription();
  }

  private class KMFileFilter
    extends javax.swing.filechooser.FileFilter
  {
    //Accept all directories and all .km/.rmdu files.
    public boolean accept( File f )
    {
      boolean rc = false;
      if ( f.isDirectory())
        rc = true;
      else
      {
        String lowerName = f.getName().toLowerCase();
        if ( lowerName.endsWith( ".km" ) || lowerName.endsWith( upgradeExtension ))
          rc = true;
      }
      return rc;
    }

    //The description of this filter
    public String getDescription()
    {
      return "RemoteMaster device upgrade files";
    }
  }

  private class KMDirectoryFilter
    extends javax.swing.filechooser.FileFilter
  {
    //Accept all directories
    public boolean accept( File f )
    {
      boolean rc = false;
      if ( f.isDirectory())
        rc = true;
      return rc;
    }

    //The description of this filter
    public String getDescription()
    {
      return "Directories";
    }
  }

  private class FileAction
    extends AbstractAction
  {
    private File file = null;
    public FileAction( File file )
    {
      super( file.getAbsolutePath());
      this.file = file;
    }

    public void actionPerformed( ActionEvent e )
    {
      try
      {
        if ( promptToSaveUpgrade( ACTION_LOAD ))
          loadUpgrade( file );
      }
      catch ( Exception ex )
      {
        ex.printStackTrace( System.err );
      }
    }

    public File getFile()
    {
      return file;
    }
  }

  private class TextFileFilter
    extends javax.swing.filechooser.FileFilter
  {
    //Accept all directories and all .km/.rmdu files.
    public boolean accept( File f )
    {
      boolean rc = false;
      if ( f.isDirectory())
        rc = true;
      else
      {
        String lowerName = f.getName().toLowerCase();
        if ( lowerName.endsWith( ".txt" ))
          rc = true;
      }
      return rc;
    }

    //The description of this filter
    public String getDescription()
    {
      return "KeyMapMaster device upgrade files";
    }
  }

  private class AnyFileFilter
    extends javax.swing.filechooser.FileFilter
  {
    //Accept all directories and all .km/.rmdu files.
    public boolean accept( File f )
    {
      boolean rc = false;
      if ( f.isDirectory())
        rc = true;
      else
      {
        String lowerName = f.getName().toLowerCase();
        if ( lowerName.endsWith( ".txt" ) || lowerName.endsWith( ".km" ) || 
             lowerName.endsWith( upgradeExtension ))
          rc = true;
      }
      return rc;
    }

    //The description of this filter
    public String getDescription()
    {
      return "All device upgrade files";
    }
  }
}
