package com.hifiremote.jp1;

import java.io.*;
import java.util.*;

public class ManualProtocol
  extends Protocol
{
  public final static int ONE_BYTE = 0;
  public final static int BEFORE_CMD = 1;
  public final static int AFTER_CMD = 2;

  public ManualProtocol( Hex id, Properties props )
  {
    super( null, id, props );
    if ( props != null )
    {
      notes = props.getProperty( "Protocol.notes" );
    }
  }

  public ManualProtocol( ManualProtocol p )
  {
    super( p.getName(), p.id, null );
    if ( p.fixedData != null )
      fixedData = new Hex( p.fixedData );
  }

  public ManualProtocol( String name, Hex id, int cmdType, String signalStyle,
                         int devBits, List< Value > parms, short[] rawHex, int cmdBits )
  {
    super( name, id, new Properties());
    System.err.println( "ManualProtocol constructor:" );
    System.err.println( "  name=" + name );
    System.err.println( "  id=" + id );
    System.err.println( "  cmdType=" + cmdType );
    System.err.println( "  signalStyle=" + signalStyle );
    System.err.println( "  devBits=" + devBits );
    System.err.println( "  parms.size()=" + parms.size());
    System.err.println( "  rawHex=" + rawHex );
    System.err.println( "  cmdBits=" + cmdBits );

    boolean lsb = false;
    boolean comp = false;
    if ( signalStyle.startsWith( "LSB" ))
      lsb = true;
    if ( signalStyle.endsWith( "COMP" ))
      comp = true;

    DirectDefaultValue defaultValue = new DirectDefaultValue( new Integer( 0 ));

    devParms = new DeviceParameter[ parms.size() ];
    deviceTranslators = new Translator[ parms.size() ];

    int offset = parms.size();
    short[] fixedBytes = new short[ offset + rawHex.length ];

    for ( int i = 0; i < parms.size(); i++ )
    {
      devParms[ i ] = new NumberDeviceParm( "Device " + ( i + 1 ), defaultValue, 10, devBits );
      System.err.println( "Setting devParms[ " + i + " ]=" + parms.get( i ));
      devParms[ i ].setValue( parms.get( i ));
      fixedBytes[ i ] = (( Integer )( parms.get( i ).getUserValue())).shortValue();
      deviceTranslators[ i ] = new Translator( lsb, comp, i, devBits, i * 8 );
    }

    for ( int i = 0 ; i < rawHex.length; i++ )
      fixedBytes[ i + offset ] = rawHex[ i ];

    fixedData = new Hex( fixedBytes );

    int byte2Index = 0;
    int cmdLength = cmdType >> 4;
    switch ( cmdType )
    {
      case ONE_BYTE:
        cmdIndex = 0;
        cmdLength = 1;
        break;
      case BEFORE_CMD:
        cmdIndex = cmdLength - 1;
        cmdLength = 2;
        byte2Index = 0;
        break;
      case AFTER_CMD:
        cmdIndex = 0;
        cmdLength = 2;
        byte2Index = 1;
        break;
      default:
        cmdIndex = 0;
        byte2Index = 0;
    }
    
    defaultCmd = new Hex( new short[ cmdLength ]);
    cmdParms = new CmdParameter[ cmdLength ];
    cmdTranslators = new Translator[ cmdLength ];
    importCmdTranslators = new Translator[ cmdLength - 1 ];
    for ( int i = 0; i < cmdLength; ++i )
    {
      if ( i == cmdIndex )
      {
        System.err.println( "Creating OBC parm & translator for index " + i + " at bit " + i * 8 );
        cmdParms[ i ] = new NumberCmdParm( "OBC", null, cmdBits );
        cmdTranslators[ i ] = new Translator( lsb, comp, cmdIndex, cmdBits, cmdIndex * 8 );
      }
      else
      {
        System.err.println( "Creating Byte " + ( i + 1 ) + " parm & translators for index " + i + " at bit " + i * 8 );
        cmdParms[ i ] = new NumberCmdParm( "Byte " + ( i + 1 ), defaultValue, cmdBits );
        cmdTranslators[ i ] = new Translator( false, false, i, 8, i * 8 );
        importCmdTranslators[ i - 1 ] = new Translator( false, false, i - 1, 8, i * 8 );
      }
    }
    /*
    cmdParms[ 0 ] = new NumberCmdParm( "OBC", null, cmdBits );
    cmdTranslators[ 0 ] = new Translator( lsb, comp, 0, cmdBits, cmdIndex * 8 );
    if ( defaultCmd.length() > 1 )
    {
      cmdParms[ 1 ] = new NumberCmdParm( "Byte 2", defaultValue, 8, 16 );
      cmdTranslators[ 1 ] = new Translator( false, false, 1, 8, byte2Index * 8 );
      importCmdTranslators = new Translator[ 1 ];
      importCmdTranslators[ 0 ] = new Translator( false, false, 0, 8, byte2Index * 8 );
    }
    */
  }

  public String getName()
  {
    if ( name != null )
      return name;
    else if ( id != null )
      return "PID " + id.toString();
    else
      return "Manual Settings";
  }

  public void setDeviceParms( List< DeviceParameter > v )
  {
    devParms = new DeviceParameter[ v.size()];
    v.toArray( devParms );
  }

  public void setDeviceTranslators( List< Translate > v )
  {
    deviceTranslators = new Translator[ v.size()];
    v.toArray( deviceTranslators );
  }

  public void setCommandParms( List< CmdParameter > v )
  {
    cmdParms = new CmdParameter[ v.size()];
    v.toArray( cmdParms );
  }

  public void setCommandTranslators( List< Translate > v )
  {
    cmdTranslators = new Translator[ v.size()];
    v.toArray( cmdTranslators );
  }

  public void importCommand( Hex hex, String text, boolean useOBC, int obcIndex, boolean useEFC )
  {
    if (( text.indexOf( ' ' ) != -1 ) || ( text.indexOf( 'h' ) != -1 ))
    {
      Hex newHex = new Hex( text );
      if ( newHex.length() > hex.length())
        setDefaultCmd( newHex );
      hex = newHex;
    }
    else
      super.importCommand( hex, text, useOBC, obcIndex, useEFC );
  }

  // for importing byte2 values from a KM upgrade.
  public void importCommandParms( Hex hex, String text )
  {
    if ( cmdParms.length == 1 )
      return;
    StringTokenizer st = new StringTokenizer( text );
    Value[] values = new Value[ st.countTokens() ];
    int index = 0;
    while ( st.hasMoreTokens())
      values[ index++ ] = new Value( Integer.valueOf( st.nextToken(), 16 ));

    for ( index = 0; index < values.length; index++ )
    {
      for ( int i = 0; i < importCmdTranslators.length; i++ )
        importCmdTranslators[ i ].in( values, hex, devParms, index );
    }
  }

  public void store( PropertyWriter out )
  {
    if ( devParms.length > 0 )
    {
      StringBuilder buff = new StringBuilder();
      for ( int i = 0; i < devParms.length; i++ )
      {
        if ( i > 0 )
          buff.append( ',' );
        DeviceParameter devParm = devParms[ i ];
        buff.append( devParm.toString() );
      }
      out.print( "DevParms", buff.toString());
    }
    if (( deviceTranslators != null ) && ( deviceTranslators.length > 0 ))
    {
      StringBuilder buff = new StringBuilder();
      for ( int i = 0; i < deviceTranslators.length; i++ )
      {
        if ( i > 0 )
          buff.append( ' ' );
        buff.append( deviceTranslators[ i ].toString());
      }
      out.print( "DeviceTranslator", buff.toString());
    }
    if ( cmdParms.length > 0 )
    {
      StringBuilder buff = new StringBuilder();
      for ( int i = 0; i < cmdParms.length; i++ )
      {
        if ( i > 0 )
          buff.append( ',' );
        buff.append( cmdParms[ i ]);
      }
      out.print( "CmdParms", buff.toString());
    }
    if ( cmdTranslators.length > 0 )
    {
      StringBuilder buff = new StringBuilder();
      for ( int i = 0; i < cmdTranslators.length; i++ )
      {
        if ( i > 0 )
          buff.append( ' ' );
        buff.append( cmdTranslators[ i ]);
      }
      out.print( "CmdTranslator", buff.toString());
    }
    out.print( "DefaultCmd", defaultCmd.toString());
    out.print( "CmdIndex", Integer.toString( cmdIndex ));
    out.print( "FixedData", fixedData.toString());
    for ( Iterator i = code.keySet().iterator(); i.hasNext(); )
    {
      Object key = i.next();
      out.print( "Code." + key, (( Hex )code.get( key )).toRawString());
    }
    if ( notes != null )
      out.print( "Protocol.notes", notes );
  }

  public void store( PropertyWriter out, Value[] vals )
    throws IOException
  {
    System.err.println( "ManualProtocol.store" );
    super.store( out, vals );
    store( out );
  }

  public void setDefaultCmd( Hex cmd )
  {
    defaultCmd = cmd;
  }

  public void setRawHex( Hex rawHex )
  {
    fixedData = rawHex;
  }

  public void setCode( Hex pCode, Processor p )
  {
    code.put( p.getEquivalentName(), pCode );
  }

  public boolean needsCode( Remote r )
  {
    return true;
  }

  public void setName( String name )
  {
    this.name = name;
  }

  public void setID( Hex newID )
  {
    id = newID;
  }

  public Hex importUpgradeCode( String notes )
  {
    Hex importedCode = super.importUpgradeCode( notes );
    if ( importedCode == null )
      return null;

    int importedCmdLength = getCmdLengthFromCode();
    // There's more bytes than we thought, so need to add another cmd parameter, translator, and importer
    if ( importedCmdLength > defaultCmd.length())
    {
      short[] newCmd = new short[ importedCmdLength ];
      defaultCmd = new Hex( newCmd );
      int newParmIndex = importedCmdLength - 1;

      CmdParameter[] newParms = new CmdParameter[ cmdParms.length + 1  ];
      Translate[] newTranslators = new Translate[ cmdTranslators.length + 1 ];
      Translate[] newImporters = new Translate[ importCmdTranslators.length + 1 ];

      System.arraycopy( cmdParms, 0, newParms, 0, cmdParms.length );
      System.arraycopy( cmdTranslators, 0, newTranslators, 0, cmdTranslators.length );
      System.arraycopy( importCmdTranslators, 0, newImporters, 0, importCmdTranslators.length );

      cmdParms = newParms;
      cmdTranslators = newTranslators;
      importCmdTranslators = newImporters;

      int newIndex = 2;
      if ( cmdIndex == 1 )
      {
        (( Translator )cmdTranslators[ 0 ] ).setBitOffset( 16 );
        newIndex = 1;
      }

      cmdParms[ newParmIndex ] = new NumberCmdParm( "Byte 3", new DirectDefaultValue( new Integer( 0 )), 8, 16 );
      cmdTranslators[ newParmIndex ] = new Translator( false, false, 2, 8, newIndex * 8 );
      importCmdTranslators[ newParmIndex - 1 ] = new Translator( false, false, 1, 8, newIndex * 8 );
    }

    return importedCode;
  }
}
