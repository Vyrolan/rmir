/**
 * 
 */
package com.hifiremote.jp1;

import java.util.List;

/**
 * @author Greg
 * 
 */
public class SoftHomeTheater extends RDFParameter
{
  private boolean use = false;
  private int deviceType = 0;
  private int deviceCode = 0;

  public void parse( String text ) throws Exception
  {
    List< String > settings = ParameterTokenizer.getTokens( text );
    use = RDFReader.parseFlag( settings.get( 0 ) );
    if ( use )
    {
      // SoftHT=Use[, DevType, DevCode]
      deviceType = RDFReader.parseNumber( settings.get( 1 ) );
      deviceCode = RDFReader.parseNumber( settings.get( 2 ) );
    }

  }

  public int getDeviceType()
  {
    return deviceType;
  }

  public int getDeviceCode()
  {
    return deviceCode;
  }
}
