package com.hifiremote.jp1;

import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * The Class AddressRange.
 */
public class AddressRange extends RDFParameter
{
  public void parse( String text ) throws Exception
  {
    StringTokenizer st = new StringTokenizer( text, ".=" );
    start = RDFReader.parseNumber( st.nextToken() );
    end = RDFReader.parseNumber( st.nextToken() );
  }

  /**
   * Gets the start.
   * 
   * @return the start
   */
  public int getStart()
  {
    return start;
  }

  /**
   * Gets the end.
   * 
   * @return the end
   */
  public int getEnd()
  {
    return end;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "$" + Integer.toHexString( start ) + "..$" + Integer.toHexString( end );
  }

  /** The start. */
  private int start;

  /** The end. */
  private int end;
}
