package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

// TODO: Auto-generated Javadoc
/**
 * The Class FunctionLabel.
 */
public class FunctionLabel
  extends JLabel
{
  
  /**
   * Instantiates a new function label.
   * 
   * @param function the function
   */
  public FunctionLabel( Function function )
  {
    if ( function == null )
      setText( "- none -" );
    else
    {
      setText( function.getName());
      if ( function.assigned() )
        showAssigned();
      else
        showUnassigned();
    }
    setHorizontalAlignment( SwingConstants.CENTER );
    Border border = 
      BorderFactory.createCompoundBorder(
        BorderFactory.createBevelBorder( BevelBorder.RAISED ),
        BorderFactory.createEmptyBorder( 2, 5, 3, 5 ));
    setBorder( border );

    if ( th == null )
    {
      th = new TransferHandler()
      {
        protected Transferable createTransferable( JComponent c )
        {
          return new LocalObjectTransferable((( FunctionLabel )c ).function );
        }

        public int getSourceActions( JComponent c )
        {
          return TransferHandler.COPY_OR_MOVE;
        }
      };
    }
    setTransferHandler( th );

    if ( ml == null )
    {
      ml = new MouseMotionAdapter()
      {
        public void mouseDragged( MouseEvent e )
        {
          JComponent c = ( JComponent )e.getSource();
          TransferHandler th = c.getTransferHandler();
          th.exportAsDrag( c, e, TransferHandler.COPY );
        }
      };
    }

    addMouseMotionListener( ml );

    this.function = function;
    updateToolTipText();
  }

  /**
   * Gets the function.
   * 
   * @return the function
   */
  public Function getFunction(){ return function; }

  /**
   * Update tool tip text.
   */
  public void updateToolTipText()
  {
    StringBuilder buff = new StringBuilder( 400 );
    buff.append( "<html>" );
    if ( function == null )
      buff.append( "&nbsp;Drag or double-click this function to<br>&nbsp;clear the function performed by a button." );
    else
    {
      if ( function.getNotes() != null )
        buff.append( "&nbsp;" + function.getNotes());
      else
        buff.append( "&nbsp;Drag or double-click this function to<br>&nbsp;set the function performed by a button." );
      java.util.List< Function.User > users = function.getUsers();
      if ( !users.isEmpty())
      {
        buff.append( "<br><hr>&nbsp;" + function.getName() + " is assigned to: " );
        boolean first = true;
        for ( Function.User user : users )
        {
          if ( first )
            first = false;
          else
            buff.append( ", " );
          Button b = user.button;
          int state = user.state;
          if ( state == Button.NORMAL_STATE )
            buff.append( b.getName());
          else if ( state == Button.SHIFTED_STATE )
            buff.append( b.getShiftedName());
          else if ( state == Button.XSHIFTED_STATE )
            buff.append( b.getXShiftedName());
        }
      }
    }
    buff.append( "</html>" );
    setToolTipText( buff.toString());
  }

  /**
   * Show assigned.
   */
  public void showAssigned()
  {
    setForeground( Color.black );
  }

  /**
   * Show unassigned.
   */
  public void showUnassigned()
  {
    setForeground( Color.red );
  }

  /* (non-Javadoc)
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize()
  {
    Dimension d = super.getPreferredSize();
    if ( d.width > 180 )
      d.width = 180;
    return d;
  }

  /** The function. */
  private Function function = null;
  
  /** The ml. */
  private static MouseMotionAdapter ml = null;
  
  /** The th. */
  private static TransferHandler th = null;
}

