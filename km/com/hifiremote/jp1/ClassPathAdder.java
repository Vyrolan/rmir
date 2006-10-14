package com.hifiremote.jp1;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

public class ClassPathAdder
{
  private static final Class[] classes = new Class[]{ URL.class };

  public static void addFile( String name ) throws IOException
  {
    addFile( new File( name ));
  }

  public static void addFile( File file )
    throws IOException
  {
    addURL( file.toURL());
  }

  public static void addURL( URL url )
    throws IOException
  {
    URLClassLoader sysloader = ( URLClassLoader )ClassLoader.getSystemClassLoader();
    Class sysclass = URLClassLoader.class;

    try
    {
      Method method = sysclass.getDeclaredMethod( "addURL", classes );
      method.setAccessible( true );
      System.err.println( "Adding to classpath: " + url.toString());
      method.invoke( sysloader, new Object[]{ url });
    }
    catch ( Throwable t )
    {
      t.printStackTrace( System.err );
      throw new IOException( "Error, could not add URL to system classloader" );
    }
  }

  public static void addFiles( File[] files )
    throws IOException
  {
    URL[] urls = new URL[ files.length ];
    for ( int i = 0; i < files.length; ++i )
      urls[ i ] = files[ i ].toURL();
    addURLs( urls );
  }

  public static void addURLs( URL[] urls )
    throws IOException
  {
    URLClassLoader sysloader = ( URLClassLoader )ClassLoader.getSystemClassLoader();
    Class sysclass = URLClassLoader.class;
    Object[] parms = new Object[ 1 ];

    try
    {
      Method method = sysclass.getDeclaredMethod( "addURL", classes );
      method.setAccessible( true );
      for ( URL url : urls )
      {
        System.err.println( "Adding to classpath: " + url.toString());
        parms[ 0 ] = url;
        method.invoke( sysloader, parms );
      }
    }
    catch (Throwable t)
    {
      t.printStackTrace();
      throw new IOException("Error, could not add URL to system classloader");
    }
  }
}
