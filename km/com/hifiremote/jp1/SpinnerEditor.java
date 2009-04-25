/**
 * 
 */
package com.hifiremote.jp1;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;

/**
 * @author Greg
 */
public class SpinnerEditor extends AbstractCellEditor implements TableCellEditor
{
  final JSpinner spinner = new JSpinner();

  public SpinnerEditor()
  {}

  public SpinnerEditor( String[] items )
  {
    spinner.setModel( new SpinnerListModel( java.util.Arrays.asList( items ) ) );
  }

  public SpinnerEditor( int min, int max )
  {
    spinner.setModel( new SpinnerNumberModel( min, min, max, 1 ) );
  }

  public void setModel( SpinnerModel model )
  {
    spinner.setModel( model );
  }

  // Prepares the spinner component and returns it.
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    spinner.setValue( value );
    return spinner;
  }

  // Enables the editor only for double-clicks.
  public boolean isCellEditable( EventObject evt )
  {
    if ( evt instanceof MouseEvent )
    {
      return ( ( MouseEvent )evt ).getClickCount() >= 2;
    }
    return true;
  }

  // Returns the spinners current value.
  public Object getCellEditorValue()
  {
    return spinner.getValue();
  }
}