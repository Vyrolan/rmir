package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class AdvancedCode.
 */
public abstract class AdvancedCode extends Highlight
{
  /** The Constant NORMAL. */
  public enum BindFormat
  {
    LONG, NORMAL
  }

  /** The Constant HEX_FORMAT. */
  public enum Format
  {
    HEX, EFC
  }

  public static AdvancedCode read( HexReader reader, Remote remote )
  {
    if ( reader.available() < 4 || reader.peek() == remote.getSectionTerminator() )
    {
      return null;
    }
    int keyCode = reader.read();
    int typeByte = reader.read();
    int type = typeByte;
    int length = 0;
    int boundDeviceIndex = 0;
    boolean isMacro = false;
    boolean isTimedMacro = false;
    boolean isFav = false;
    int sequenceNumber = 0;
    if ( remote.getAdvCodeBindFormat() == BindFormat.NORMAL )
    {
      length = type & 0x0F;
      type >>= 4;
      boundDeviceIndex = type >> 1;
      if ( type == 1 )
      {
        isMacro = true;
      }
      else if ( type == 3 )
      {
        isFav = true;
        FavKey favKey = remote.getFavKey();
        if ( favKey != null )
        {
          length *= favKey.getEntrySize();
        }
      }
    }
    else
    {
      length = reader.read();
      boundDeviceIndex = type & 0x0F;
      type >>= 4;
      if ( remote.getMacroCodingType().getType() == 2 )
      {
        if ( type >= 3 )
        {
          if ( ( type & 8 ) == 8 && remote.hasTimedMacroSupport() )
          {
            isTimedMacro = true;
          }
          else
          {
            isMacro = true;
            sequenceNumber = type - 3;
          }
        }
      }
      else
      {
        if ( ( type & 8 ) == 8 )
        {
          isMacro = true;
          sequenceNumber = type & 0x07;
        }
        else if ( ( type & 3 ) == 3 )
        {
          isFav = true;
        }
      }
    }

    System.err.println( "length=" + length );
    Hex hex = new Hex( reader.read( length ) );

    if ( isMacro )
    {
      Macro macro = new Macro( keyCode, hex, null );
      macro.setSequenceNumber( sequenceNumber );
      macro.setDeviceIndex( boundDeviceIndex );
      return macro;
    }
    else if ( isFav )
    {
      FavScan favScan = new FavScan( keyCode, hex, null );
      favScan.setDeviceIndex( boundDeviceIndex );
      return favScan;
    }
    else if ( isTimedMacro )
    {
      TimedMacro timedMacro = new TimedMacro( keyCode, typeByte, hex, null );
      return timedMacro;
    }
    else
    {
      KeyMove keyMove = null;
      if ( ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG 
          || remote.getAdvCodeFormat() == AdvancedCode.Format.EFC )
          && length == 3 )
      {
        keyMove = new KeyMoveKey( keyCode, boundDeviceIndex, hex, null );
      }
      else if ( remote.getAdvCodeFormat() == AdvancedCode.Format.HEX )
      {
        keyMove = new KeyMove( keyCode, boundDeviceIndex, hex, null );
      }
      else
      {
        if ( remote.getEFCDigits() == 3 )
        {
          keyMove = new KeyMoveEFC( keyCode, boundDeviceIndex, hex, null );
        }
        else
        // EFCDigits == 5
        {
          keyMove = new KeyMoveEFC5( keyCode, boundDeviceIndex, hex, null );
        }
      }
      return keyMove;
    }
  }

  /**
   * Instantiates a new advanced code.
   * 
   * @param keyCode
   *          the key code
   * @param data
   *          the data
   * @param notes
   *          the notes
   */
  public AdvancedCode( int keyCode, Hex data, String notes )
  {
    this.keyCode = keyCode;
    this.data = data;
    this.notes = notes;
  }

  /**
   * Instantiates a new advanced code.
   * 
   * @param props
   *          the props
   */
  public AdvancedCode( Properties props )
  {
    super( props );
    // Allow for missing "KeyCode" entry, as it is not used by Timed Macros
    String temp = props.getProperty( "KeyCode" );
    keyCode = temp == null ? 0 : Integer.parseInt( temp );
    data = new Hex( props.getProperty( "Data" ) );
    notes = props.getProperty( "Notes" );
  }

  /** The key code. */
  protected int keyCode;

  /**
   * Gets the key code.
   * 
   * @return the key code
   */
  public int getKeyCode()
  {
    return keyCode;
  }
  
  /**
   * Sets the key code.
   * 
   * @param keyCode
   *          the new key code
   */
  public void setKeyCode( int keyCode )
  {
    if ( this.keyCode != keyCode )
    {
      this.keyCode = keyCode;
    }
  }
  
  public int getSize( Remote remote )
  {
    int size = data.length() + 2; // for the key code and type/length
    if ( remote.getAdvCodeBindFormat() == BindFormat.LONG )
    {
      size++ ; // length is stored in it's own byte, not with the type;
    }
    return size;
  }

  public abstract int store( short[] buffer, int offset, Remote remote );

  /**
   * Gets the value string.
   * 
   * @param remoteConfig
   *          the remote config
   * @return the value string
   */
  public abstract String getValueString( RemoteConfiguration remoteConfig );

  /** The data. */
  protected Hex data;

  /**
   * Gets the data.
   * 
   * @return the data
   */
  public Hex getData()
  {
    return data;
  }

  /**
   * Sets the data.
   * 
   * @param hex
   *          the new data
   */
  public void setData( Hex hex )
  {
    data = hex;
  }

  /** The notes. */
  protected String notes = null;

  /**
   * Gets the notes.
   * 
   * @return the notes
   */
  public String getNotes()
  {
    return notes;
  }

  /**
   * Sets the notes.
   * 
   * @param notes
   *          the new notes
   */
  public void setNotes( String notes )
  {
    if ( notes != this.notes && ( notes == null || !notes.equals( this.notes ) ) )
    {
      this.notes = notes;
    }
  }

  /**
   * Store.
   * 
   * @param pw
   *          the pw
   */
  public void store( PropertyWriter pw )
  {
    super.store( pw );
    pw.print( "KeyCode", keyCode );
    pw.print( "Data", data );
    if ( notes != null && notes.length() > 0 )
    {
      pw.print( "Notes", notes );
    }
  }
}
