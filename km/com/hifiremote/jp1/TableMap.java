/** 
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap 
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting 
 * a TableMap which has not been subclassed into a chain of table filters 
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne */

package com.hifiremote.jp1;

import javax.swing.table.*; 
import javax.swing.event.TableModelListener; 
import javax.swing.event.TableModelEvent; 

public class TableMap extends JP1TableModel 
                      implements TableModelListener {
    protected JP1TableModel model; 

    public TableModel getModel() {
        return model;
    }

    public void setModel( JP1TableModel model ) {
        this.model = model; 
        model.addTableModelListener( this ); 
    }

    // By default, implement TableModel by forwarding all messages 
    // to the model. 

    public Object getValueAt(int aRow, int aColumn) {
        return model.getValueAt(aRow, aColumn); 
    }
        
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        model.setValueAt(aValue, aRow, aColumn); 
    }

    public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount(); 
    }

    public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount(); 
    }
        
    public String getColumnName(int aColumn) 
    {
        return model.getColumnName(aColumn); 
    }
    
    public String getColumnPrototypeName( int col )
    {
      return model.getColumnPrototypeName( col );
    }

    public Class getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn); 
    }
        
    public boolean isCellEditable(int row, int column) { 
         return model.isCellEditable(row, column); 
    }
    
    public boolean isColumnWidthFixed( int col )
    {
      return model.isColumnWidthFixed( col );
    }

    public TableCellRenderer getColumnRenderer( int col )
    {
      return model.getColumnRenderer( col );
    }
    
    public TableCellEditor getColumnEditor( int col )
    {
      return model.getColumnEditor( col );
    }

//
// Implementation of the TableModelListener interface, 
//
    // By default forward all events to all the listeners. 
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }

    public void addRow( Object object )
    {
      model.addRow( object );
    }

    public void insertRow( int row, Object object )
    {
      model.insertRow( row, object );
    }

    public void removeRow( int row )
    {
      model.removeRow( row );
    }

    public Object getRow( int row )
    {
      return model.getRow( row );
    }

    public void moveRow( int from, int to )
    {
      model.moveRow( from, to );
    }
}
