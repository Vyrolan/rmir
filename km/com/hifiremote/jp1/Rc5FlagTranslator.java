package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class Rc5FlagTranslator.
 */
public class Rc5FlagTranslator
  extends Translate
{
  
  /**
   * Instantiates a new rc5 flag translator.
   * 
   * @param textParms the text parms
   */
  public Rc5FlagTranslator( String[] textParms )
  {
    super( textParms );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex, com.hifiremote.jp1.DeviceParameter[], int)
   */
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    short[] hex = hexData.getData();
    int numFlags = hex.length;
    int flag = 0;
    for ( int i = 0; i < numFlags; i++ )
    {
      int parmIndex = 2 * i;
      Object val = parms[ parmIndex ].getUserValue();
      int thisFlag = (( Number )parms[ parmIndex + 1 ].getValue()).intValue();
      if (( i != 0 ) && ( val == null ))
        thisFlag = 1 - flag;

      hex[ i ] = ( short )(( hex[ i ] & 0xBF) | ( thisFlag * 0x40 ));
      flag = thisFlag;
    }
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[], com.hifiremote.jp1.DeviceParameter[])
   */
  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    short[] hex = hexData.getData();

    Value one = new Value( new Integer( 1 ));
    Value zero = new Value( new Integer( 0 ));
    for ( int i = 0; i < hex.length; i++ )
    {
      int flag = hex[ i ] & 0x40;
      int parmIndex = ( 2 * i ) + 1;
      parms[ parmIndex ] = ( flag == 0 ) ? zero : one;
    }
  }
}
