package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.hifiremote.jp1.Activity.Assister;
import com.hifiremote.jp1.RemoteConfiguration.Icon;
import com.hifiremote.jp1.RemoteMaster.IconImage;

public class ActivityPanel extends RMPanel implements ChangeListener, ActionListener, ListSelectionListener
{
  public ActivityPanel()
  {
    super();
    tabPanel = new JPanel ( new BorderLayout() );
    add( tabPanel, BorderLayout.CENTER );
    JPanel panel = new JPanel( new BorderLayout() );
    panel.setBorder( BorderFactory.createTitledBorder( "Activity Functions" ) );
    activityFunctionTable = new JP1Table( activityFunctionModel );
    activityFunctionTable.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    activityFunctionModel.setPanel( this );
    JScrollPane scrollPane = new JScrollPane( activityFunctionTable );
    JPanel upper = new JPanel( new BorderLayout() );
    upper.add( scrollPane, BorderLayout.CENTER );
    messageArea = new JTextArea();
    JLabel label = new JLabel();
    messageArea.setFont( label.getFont() );
    messageArea.setBackground( label.getBackground() );
    messageArea.setLineWrap( true );
    messageArea.setWrapStyleWord( true );
    messageArea.setEditable( false );
    messageArea.setBorder( BorderFactory.createEmptyBorder( 5, 5, 10, 5 ) );
    upper.add( messageArea, BorderLayout.PAGE_END );
    panel.add( upper, BorderLayout.CENTER );
    tabPanel.add( panel, BorderLayout.PAGE_START );
    
    JPanel inner = new JPanel( new BorderLayout() );

    for ( int i = 0; i < 3; i++ )
    {   
      panel = new JPanel( new BorderLayout() );
      panel.setBorder( BorderFactory.createTitledBorder( Activity.assistType[ i ] + " Assist" ) );
      activityAssistModels[ i ] = new ActivityAssistTableModel();
      activityAssistTables[ i ] = new JP1Table( activityAssistModels[ i ] );
      activityAssistTables[ i ].setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
      activityAssistTables[ i ].getSelectionModel().addListSelectionListener( this );
      scrollPane = new JScrollPane( activityAssistTables[ i ] );
      panel.add( scrollPane, BorderLayout.CENTER );
      Dimension dd = activityAssistTables[ i ].getPreferredSize();
      dd.height = 4 * activityAssistTables[ i ].getRowHeight();
      activityAssistTables[ i ].setPreferredScrollableViewportSize( dd );
      newAssist[ i ] = new JButton( "New");
      newAssist[ i ].addActionListener( this );
      deleteAssist[ i ] = new JButton( "Delete" );
      deleteAssist[ i ].addActionListener( this );
      deleteAssist[ i ].setEnabled( false );
      JPanel btnPanel = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
      btnPanel.add( newAssist[ i ] );
      btnPanel.add( deleteAssist[ i ] );
      panel.add( btnPanel, BorderLayout.PAGE_END );
      grid.add( panel );
    }
    inner.add( grid, BorderLayout.PAGE_START );
    grid.setVisible( false );
    
    panel = new JPanel( new BorderLayout() );
    panel.setBorder( BorderFactory.createTitledBorder( "Activity Group Assignments" ) );
    activityGroupTable = new JP1Table( activityGroupModel );
    activityGroupTable.setCellEditorModel( activityGroupModel );
    activityGroupTable.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    activityFunctionModel.setActivityGroupModel( activityGroupModel );
    scrollPane = new JScrollPane( activityGroupTable );
    panel.add( scrollPane, BorderLayout.CENTER );
    
    inner.add( panel, BorderLayout.CENTER);
    
    tabPanel.add( inner, BorderLayout.CENTER );
    panel = new JPanel( new FlowLayout( FlowLayout.CENTER, 5, 0 ) );
    clearActivity = new JButton( "Clear Activity" );
    clearActivity.addActionListener( this );
    panel.add( clearActivity );
    deleteActivity = new JButton( "Delete Activity" );
    deleteActivity.addActionListener( this );
    panel.add( deleteActivity );
    newActivity = new JButton( "New Activity" );
    newActivity.addActionListener( this );
    panel.add( newActivity );
    
    iconImage = new IconImage();
    panel.add( Box.createHorizontalStrut( 10 ) );
    panel.add( iconImage );
    add( panel, BorderLayout.PAGE_END );
    tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener( this );    
    add( tabbedPane, BorderLayout.CENTER );
    Dimension d = activityFunctionTable.getPreferredSize();
    d.height = 2 * activityFunctionTable.getRowHeight();
    activityFunctionTable.setPreferredScrollableViewportSize( d );
    activityFunctionTable.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusGained( FocusEvent e )
      {
        activeTable = activityFunctionTable;
        setHighlightAction( activityFunctionTable );
      }
    } );
    activityGroupTable.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusGained( FocusEvent e )
      {
        activeTable = activityGroupTable;
        setHighlightAction( activityGroupTable );
      }
    } );
    activeTable = activityFunctionTable;
  }
  
  private void setHighlightAction( JP1Table table )
  {
    remoteConfig.getOwner().highlightAction.setEnabled( table.getSelectedRowCount() > 0 );
  }
  
  public JP1Table getActiveTable()
  {
    return activeTable;
  }
  
  @Override
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    if ( listener != null )
    {
      if ( activityGroupModel != null )
      {
        activityGroupModel.addPropertyChangeListener( listener );
      }
      if ( activityFunctionModel != null )
      {
        activityFunctionModel.addPropertyChangeListener( listener );
      }
      for ( int i = 0; i < 3; i++ )
      {
        if ( activityAssistModels[ i ] != null )
        {
          activityAssistModels[ i ].addPropertyChangeListener( listener );
        }
      }
      propertyChangeSupport.addPropertyChangeListener( listener );
    }
  }
  
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );

  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    Remote remote = null;
    if ( remoteConfig != null && ( remote = remoteConfig.getRemote() ).getButtonGroups() != null
        && remote.getButtonGroups().keySet().contains( "Activity" ) )
    {
      newActivity.setVisible( remote.usesEZRC() );
      deleteActivity.setVisible( remote.usesEZRC() );
      clearActivity.setVisible( !remote.usesEZRC() );
      String startMessage = "Note:  When the activity has been set with the remote, \"Key\" is "
        + "the number key pressed to select the desired combination for the activity.  If "
        + "\"Key\" is blank, the activity has not been set.  The \"Key\" value has no "
        + "significance when the activity is set with RMIR, but some value has to be set "
        + "for it before ";
      if ( remote.usesEZRC() )
      {
        messageArea.setText( "Note:  Size and highlight color for the Activity Functions includes "
            + "those of the Activity Assists." );
      }
      else if ( remote.hasMasterPowerSupport() )
      {
        messageArea.setText( startMessage + "a Power Macro can be entered." );
      }
      else if ( remote.hasActivityControl() )
      {
        messageArea.setText( startMessage + "Activity Group Assignments can be edited." );
      }
      messageArea.setVisible( remote.hasMasterPowerSupport() || remote.hasActivityControl() );
      tabbedPane.removeAll();
      activityList.clear();
      lastIndex = 0;
      int index = 0;
      boolean allowNew = false;
      boolean allowDelete = false;
      for ( Button btn : remote.getButtonGroups().get( "Activity" ) )
      {
        Activity activity = remoteConfig.getActivities().get( btn );
        if ( activity == null )
        {
          continue;
        }
        if ( activity.isActive() )
        {
          activityList.add( activity );
          tabbedPane.addTab( activity.getName(), null );
          if ( activity.isNew() )
          {
            tabbedPane.setSelectedIndex( index );
            activity.setNew( false );
          }
          allowDelete = true;
          index++;
        }
        else
        {
          allowNew = true;
        }
      }
      newActivity.setEnabled( allowNew );
      deleteActivity.setEnabled( allowDelete );
    }
  }

  private List< Activity > activityList = new ArrayList< Activity >();
  private JPanel grid = new JPanel( new GridLayout( 1, 3 ) );
  private JP1Table activityGroupTable = null;
  private ActivityGroupTableModel activityGroupModel = new ActivityGroupTableModel();
  private JP1Table activityFunctionTable = null;
  private ActivityFunctionTableModel activityFunctionModel = new ActivityFunctionTableModel();
  private JP1Table[] activityAssistTables = { null, null, null };
  private ActivityAssistTableModel[] activityAssistModels = { null, null, null };
  private JPanel tabPanel = null;
  private JTabbedPane tabbedPane = null;
  private RemoteConfiguration remoteConfig = null;
  private int lastIndex = 0;
  private JP1Table activeTable = null;
  private JButton clearActivity = null;
  private JButton newActivity = null;
  private JButton deleteActivity = null;
  private JButton[] newAssist = new JButton[ 3 ];
  private JButton[] deleteAssist = new JButton[ 3 ];
  private JTextArea messageArea = null;
  private IconImage iconImage = null;

  @Override
  public void stateChanged( ChangeEvent e )
  {
    if ( e.getSource() == tabbedPane )
    {
      int index = tabbedPane.getSelectedIndex();
      if ( index < 0 )
      {
        return;
      }

      tabbedPane.setComponentAt( lastIndex, null );

      if ( tabbedPane.getComponentAt( index ) == null )
      {
        tabbedPane.setComponentAt( index, tabPanel );
        lastIndex = index;
      }
      Activity activity = activityList.get( index );
      Button btn = activity.getButton();
      finishEditing();
      activityFunctionModel.set( btn, remoteConfig );
      activityFunctionTable.initColumns( activityFunctionModel );
      if ( remoteConfig != null && remoteConfig.getRemote().usesEZRC() )
      {
        grid.setVisible( true );
        for ( int i = 0; i < 3; i++ )
        {
          activityAssistModels[ i ].set( btn, i, remoteConfig );
          activityAssistTables[ i ].initColumns( activityAssistModels[ i ] );
          if ( i < 2 )
          {
            Activity a = activityList.get( index );
            newAssist[ i ].setEnabled( a.getAssists().size() <= i || a.getAssists().get( i ).isEmpty() );
          }
        }
        
        BufferedImage image = null;
        Integer iconref = activity.getIconref();
        if ( iconref != null && remoteConfig.getSysIcons() != null && iconref < 127 )
        {
          Icon icon = remoteConfig.getSysIcons().get( iconref );
          image = icon != null ? icon.image : null;
        }
        iconImage.setImage( image );
      }
      else
      {
        grid.setVisible( false );
      }
      activityGroupModel.set( btn, remoteConfig, null );
      activityGroupTable.initColumns( activityGroupModel );
      repaint();
    }
  }
  
  public void finishEditing()
  {
    if ( activityFunctionTable.getCellEditor() != null )
    {
      activityFunctionTable.getCellEditor().stopCellEditing();
    }
    if ( activityGroupTable.getCellEditor() != null )
    {
      activityGroupTable.getCellEditor().stopCellEditing();
    }
    for ( int i = 0; i < 3; i++ )
    {
      if ( activityAssistTables[ i ].getCellEditor() != null )
      {
        activityAssistTables[ i ].getCellEditor().stopCellEditing();
      }
    }
  }
  
  public void setTabTitle( String title )
  {
    tabbedPane.setTitleAt( tabbedPane.getSelectedIndex(), title );
  }

  @Override
  public void actionPerformed( ActionEvent e )
  {
    finishEditing();
    Remote remote = remoteConfig.getRemote();
    Object source = e.getSource();
    boolean tabChange = false;
    int index = 0;
    if ( source == clearActivity || source == deleteActivity )
    {
      Activity activity = activityFunctionModel.getRow( 0 );
      activity.setSelector( null );
      activity.setAudioHelp( 1 );
      activity.setVideoHelp( 1 );
      activity.setHelpSegment( null );
      for ( ActivityGroup group : activity.getActivityGroups() )
      {
        group.setDevice( DeviceButton.noButton );
      }
      for ( int i = 0; i < activity.getAssists().size(); i++ )
      {
        activity.getAssists().get( i ).clear();
      }
      if ( source == deleteActivity )
      {
        activity.getMacro().setData( new Hex( 0 ) );
        activity.setActive( false );
        tabChange = true;
      }
      else
      {
        activity.setMacro( null );
      }
    }
    else if ( source == newActivity )
    {
      Activity activity = null;
      List< Button > freeBtns = new ArrayList< Button >( remote.getButtonGroups().get( "Activity" ) );
      for ( Activity a : remoteConfig.getActivities().values() )
      {
        if ( a.isActive() )
        {
          freeBtns.remove( a.getSelector() );
        }
        else if ( activity == null )
        {
          activity = a;
        }
      }
      activity.setActive( true );
      activity.setNew( true );
      activity.setSelector( freeBtns.get( 0 ) );
      
//      Macro macro = new Macro( activity.getButton().getKeyCode(), new Hex( 0 ), activity.getSelector().getKeyCode(), 0, null );
//      macro.setSegmentFlags( 0xFF );
//      activity.setMacro( macro );
      

      activity.setName( "New Activity" );
      tabChange = true;
    }
    else if ( ( index = Arrays.asList( newAssist ).indexOf( source ) ) >= 0 )
    {
      Activity activity = activityFunctionModel.getRow( 0 );
      if ( activity.getAssists().isEmpty() )
      {
        for ( int i = 0; i < 3; i++ )
        {
          activity.getAssists().put( i, new ArrayList< Assister >() );
        }
        tabChange = true;
      }
      List< Assister > assists = activity.getAssists().get( index );
      DeviceButton dev = remote.getDeviceButtons()[ 0 ];
      Button btn = remote.getUpgradeButtons()[ 0 ];
      assists.add( new Assister( dev, btn ) );
      if ( index < 2 )
      {
        newAssist[ index ].setEnabled( false );
      }
    }
    else if ( ( index = Arrays.asList( deleteAssist ).indexOf( source ) ) >= 0 )
    {
      Activity activity = activityFunctionModel.getRow( 0 );
      List< Assister > assists = activity.getAssists().get( index );
      assists.remove( activityAssistTables[ index ].getSelectedRow() );
      if ( index < 2 )
      {
        newAssist[ index ].setEnabled( true );
      }
    }
    activityFunctionModel.fireTableDataChanged();
    activityGroupModel.fireTableDataChanged();
    for ( int i = 0; i < 3; i++ )
    {
      activityAssistModels[ i ].fireTableDataChanged();
    }
    propertyChangeSupport.firePropertyChange( tabChange ? "tabs" : "data", null, null );  
  }

  @Override
  public void valueChanged( ListSelectionEvent e )
  {
    if ( e.getValueIsAdjusting() )
    {
      return;
    }
    
    Object source = e.getSource();
    int i;
    for ( i = 0; i < 3; i++ )
    {
      if ( activityAssistTables[ i ].getSelectionModel() == source )
      {
        break;
      }
    }
    if ( i == 3 )
    {
      return;
    }
    deleteAssist[ i ].setEnabled( activityAssistTables[ i ].getSelectedRow() >= 0 );
  }
  
}
