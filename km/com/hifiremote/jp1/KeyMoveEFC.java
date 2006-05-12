package com.hifiremote.jp1;

public class KeyMoveEFC
  extends KeyMove
{
  public KeyMoveEFC( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, deviceButtonIndex, data, notes );
  }

  public Object getValue()
  {
    return new EFC(( short )data.get( CMD_INDEX ));
  }

  public void setValue( Object value )
  {
    data.put((( EFC )value ).getValue(), CMD_INDEX );
  }
}
