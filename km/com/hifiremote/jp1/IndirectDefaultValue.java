package com.hifiremote.jp1;

public class IndirectDefaultValue
 extends DefaultValue
{
  public IndirectDefaultValue( int index, Parameter ref )
  {
    this.index = index;
    this.ref = ref;
  }

  public void setIsComplement( boolean flag )
  {
    System.err.println( "IndirectDefaultValue.setIsComplement( " + flag + " )" );
    complement = true;
  }

  public boolean getIsComplement()
  {
    return complement;
  }

  public Object value()
  {
    Integer rc = ( Integer )ref.getValueOrDefault();
    if ( complement )
      rc = new Integer( 255 - rc.intValue());
      
    System.err.println( "IndirectDefaultValue.value() returns " + rc );
    return rc;
  }

  private Parameter ref;
  private int index = 0;
  private boolean complement = false;

  public String toString(){ return "[" + ( complement ? "-" : "" ) + index + "]"; }
}