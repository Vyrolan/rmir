package com.hifiremote.jp1;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class LearnedSignalPanel
  extends RMTablePanel
{
  public LearnedSignalPanel()
  {
    super( new LearnedSignalTableModel());
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    (( LearnedSignalTableModel )model ).set( remoteConfig ); 
  }
  
  public Object createRowObject()
  {
    return null;
  }
}
  
