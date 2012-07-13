/*
 * Copyright (c) 2002-2012, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.portal.service.search;

import fr.paris.lutece.portal.business.indexeraction.IndexerAction;
import fr.paris.lutece.portal.business.indexeraction.IndexerActionFilter;
import fr.paris.lutece.portal.business.indexeraction.IndexerActionHome;
import fr.paris.lutece.portal.service.init.LuteceInitException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.commons.lang.StringUtils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class provides management methods for indexing
 */
public final class IndexationService
{
    // Constants corresponding to the variables defined in the lutece.properties file
    public static final String PATH_INDEX = "search.lucene.indexPath";
    public static final String PARAM_FORCING = "forcing";
    public static final int ALL_DOCUMENT = -1;
    public static Version LUCENE_INDEX_VERSION = Version.LUCENE_29;
    private static final String PARAM_TYPE_PAGE = "Page";
    private static final String PROPERTY_WRITER_MERGE_FACTOR = "search.lucene.writer.mergeFactor";
    private static final String PROPERTY_WRITER_MAX_FIELD_LENGTH = "search.lucene.writer.maxFieldLength";
    private static final String PROPERTY_ANALYSER_CLASS_NAME = "search.lucene.analyser.className";
    private static final int DEFAULT_WRITER_MERGE_FACTOR = 20;
    private static final int DEFAULT_WRITER_MAX_FIELD_LENGTH = 1000000;
    private static String _strIndex;
    private static int _nWriterMergeFactor;
    private static int _nWriterMaxFieldLength;
    private static Analyzer _analyzer;
    private static Map<String, SearchIndexer> _mapIndexers = new HashMap<String, SearchIndexer>(  );
    private static IndexationService _singleton;
    private static IndexWriter _writer;
    private static StringBuffer _sbLogs;

    /**
     * The private constructor
     */
    private IndexationService(  )
    {
    }

    /**
     *
     * @return singleton
     */
    public static IndexationService getInstance(  )
    {
        if ( _singleton == null )
        {
            _singleton = new IndexationService(  );
        }

        return _singleton;
    }

    /**
     * Initalizes the service
     * @throws LuteceInitException If an error occured
     */
    public static void init(  ) throws LuteceInitException
    {
        // Read configuration properties
        _strIndex = AppPathService.getPath( PATH_INDEX );

        if ( ( _strIndex == null ) || ( _strIndex.equals( "" ) ) )
        {
            throw new LuteceInitException( "Lucene index path not found in lucene.properties", null );
        }

        _nWriterMergeFactor = AppPropertiesService.getPropertyInt( PROPERTY_WRITER_MERGE_FACTOR,
                DEFAULT_WRITER_MERGE_FACTOR );
        _nWriterMaxFieldLength = AppPropertiesService.getPropertyInt( PROPERTY_WRITER_MAX_FIELD_LENGTH,
                DEFAULT_WRITER_MAX_FIELD_LENGTH );

        String strAnalyserClassName = AppPropertiesService.getProperty( PROPERTY_ANALYSER_CLASS_NAME );

        if ( ( _strIndex == null ) || ( _strIndex.equals( "" ) ) )
        {
            throw new LuteceInitException( "Analyser class name not found in lucene.properties", null );
        }

        try
        {
            _analyzer = (Analyzer) Class.forName( strAnalyserClassName ).newInstance(  );
        }
        catch ( Exception e )
        {
            throw new LuteceInitException( "Failed to load Lucene Analyzer class", e );
        }
    }

    /**
     * Register an indexer
     * @param indexer The indexer to add to the registry
     */
    public static void registerIndexer( SearchIndexer indexer )
    {
        if ( indexer instanceof SearchIndexer )
        {
            _mapIndexers.put( indexer.getName(  ), indexer );
            AppLogService.info( "New search indexer registered : " + indexer.getName(  ) );
        }
        else
        {
            AppLogService.info( "Bad search indexer not registered : " + indexer.getName(  ) );
        }
    }

    /**
     * Process the indexing
     * @param bCreate Force creating the index
     * @return the result log of the indexing
     */
    public static synchronized String processIndexing( boolean bCreate )
    {
        // String buffer for building the response page;
        _sbLogs = new StringBuffer(  );

        _writer = null;

        boolean bCreateIndex = bCreate;

        try
        {
            Directory dir = IndexationService.getDirectoryIndex(  );

            if ( !IndexReader.indexExists( dir ) )
            { //init index
                bCreateIndex = true;
            }

            Date start = new Date(  );
            _writer = new IndexWriter( dir, _analyzer, bCreateIndex, IndexWriter.MaxFieldLength.UNLIMITED );
            _writer.setMergeFactor( _nWriterMergeFactor );
            _writer.setMaxFieldLength( _nWriterMaxFieldLength );

            if ( bCreateIndex )
            {
                _sbLogs.append( "\r\nIndexing all contents ...\r\n" );

                for ( SearchIndexer indexer : _mapIndexers.values(  ) )
                {
                    // catch any exception coming from an indexer to prevent global indexation to fail
                    try
                    {
                        if ( indexer.isEnable(  ) )
                        {
                            _sbLogs.append( "\r\n<strong>Indexer : " );
                            _sbLogs.append( indexer.getName(  ) );
                            _sbLogs.append( " - " );
                            _sbLogs.append( indexer.getDescription(  ) );
                            _sbLogs.append( "</strong>\r\n" );

                            //the indexer will call write(doc)
                            indexer.indexDocuments(  );
                        }
                    }
                    catch ( Exception e )
                    {
                        error( indexer, e, StringUtils.EMPTY );
                    }
                }

                removeAllIndexerAction(  );
            }
            else
            {
                _sbLogs.append( "\r\nIncremental Indexing ...\r\n" );

                //incremental indexing
                Collection<IndexerAction> actions = IndexerActionHome.getList(  );

                /*if(actions.size() < 1)
                {
                _sbLogs.append("\n Nothing to index\n");
                }*/
                for ( IndexerAction action : actions )
                {
                    // catch any exception coming from an indexer to prevent global indexation to fail
                    try
                    {
                        SearchIndexer indexer = _mapIndexers.get( action.getIndexerName(  ) );

                        if ( action.getIdTask(  ) == IndexerAction.TASK_DELETE )
                        {
                            if ( action.getIdPortlet(  ) != ALL_DOCUMENT )
                            {
                                //delete only the index linked to this portlet
                                _writer.deleteDocuments( new Term( SearchItem.FIELD_DOCUMENT_PORTLET_ID,
                                        action.getIdDocument(  ) + "&" + Integer.toString( action.getIdPortlet(  ) ) ) );
                            }
                            else
                            {
                                //delete all index linked to uid
                                _writer.deleteDocuments( new Term( SearchItem.FIELD_UID, action.getIdDocument(  ) ) );
                            }

                            _sbLogs.append( "Deleting " );
                            _sbLogs.append( " #" );
                            _sbLogs.append( action.getIdDocument(  ) );
                            _sbLogs.append( "\r\n" );
                        }
                        else
                        {
                            List<org.apache.lucene.document.Document> luceneDocuments = indexer.getDocuments( action.getIdDocument(  ) );

                            if ( ( luceneDocuments != null ) && ( luceneDocuments.size(  ) > 0 ) )
                            {
                                for ( org.apache.lucene.document.Document doc : luceneDocuments )
                                {
                                    if ( ( action.getIdPortlet(  ) == ALL_DOCUMENT ) ||
                                            ( ( doc.get( SearchItem.FIELD_DOCUMENT_PORTLET_ID ) != null ) &&
                                            ( doc.get( SearchItem.FIELD_DOCUMENT_PORTLET_ID )
                                                     .equals( doc.get( SearchItem.FIELD_UID ) + "&" +
                                                action.getIdPortlet(  ) ) ) ) )
                                    {
                                        if ( action.getIdTask(  ) == IndexerAction.TASK_CREATE )
                                        {
                                            _writer.addDocument( doc );
                                            _sbLogs.append( "Adding " );
                                        }
                                        else if ( action.getIdTask(  ) == IndexerAction.TASK_MODIFY )
                                        {
                                            if ( action.getIdPortlet(  ) != ALL_DOCUMENT )
                                            {
                                                //delete only the index linked to this portlet
                                                _writer.updateDocument( new Term( 
                                                        SearchItem.FIELD_DOCUMENT_PORTLET_ID,
                                                        doc.get( SearchItem.FIELD_DOCUMENT_PORTLET_ID ) ), doc );
                                                _sbLogs.append( "Updating " );
                                            }
                                            else
                                            {
                                                _writer.updateDocument( new Term( SearchItem.FIELD_UID,
                                                        doc.getField( SearchItem.FIELD_UID ).stringValue(  ) ), doc );
                                                _sbLogs.append( "Updating " );
                                            }
                                        }

                                        _sbLogs.append( doc.get( SearchItem.FIELD_TYPE ) );
                                        _sbLogs.append( " #" );
                                        _sbLogs.append( doc.get( SearchItem.FIELD_UID ) );
                                        _sbLogs.append( " - " );
                                        _sbLogs.append( doc.get( SearchItem.FIELD_TITLE ) );
                                        _sbLogs.append( "\r\n" );
                                    }
                                }
                            }
                        }

                        removeIndexerAction( action.getIdAction(  ) );
                    }
                    catch ( Exception e )
                    {
                        error( action, e, StringUtils.EMPTY );
                    }
                }

                //reindexing all pages.
                _writer.deleteDocuments( new Term( SearchItem.FIELD_TYPE, PARAM_TYPE_PAGE ) );
                _mapIndexers.get( PageIndexer.INDEXER_NAME ).indexDocuments(  );
            }

            _sbLogs.append( "\r\nOptimization of the index for the current site...\r\n\r\n" );
            _writer.optimize(  );

            Date end = new Date(  );
            _sbLogs.append( "Duration of the treatment : " );
            _sbLogs.append( end.getTime(  ) - start.getTime(  ) );
            _sbLogs.append( " milliseconds\r\n" );
        }
        catch ( Exception e )
        {
            _sbLogs.append( " caught a " );
            _sbLogs.append( e.getClass(  ) );
            _sbLogs.append( "\n with message: " );
            _sbLogs.append( e.getMessage(  ) );
            _sbLogs.append( "\r\n" );
            AppLogService.error( "Indexing error : " + e.getMessage(  ), e );
        }
        finally
        {
            try
            {
                if ( _writer != null )
                {
                    _writer.close(  );
                }
            }
            catch ( IOException e )
            {
                AppLogService.error( e.getMessage(  ), e );
            }
        }

        return _sbLogs.toString(  );
    }

    /**
     * Index one document, called by plugin indexers
     * @param doc the document to index
     * @throws CorruptIndexException corruptIndexException
     * @throws IOException i/o exception
     */
    public static void write( Document doc ) throws CorruptIndexException, IOException
    {
        _writer.addDocument( doc );
        _sbLogs.append( "Indexing " );
        _sbLogs.append( doc.get( SearchItem.FIELD_TYPE ) );
        _sbLogs.append( " #" );
        _sbLogs.append( doc.get( SearchItem.FIELD_UID ) );
        _sbLogs.append( " - " );
        _sbLogs.append( doc.get( SearchItem.FIELD_TITLE ) );
        _sbLogs.append( "\r\n" );
    }

    /**
     * Log the error for the search indexer
     * @param indexer the {@link SearchIndexer}
     * @param e the exception
     */
    public static void error( SearchIndexer indexer, Exception e, String strMessage )
    {
        _sbLogs.append( "<strong class=\"alert\">Indexer : " );
        _sbLogs.append( indexer.getName(  ) );
        _sbLogs.append( " - ERROR : " );
        _sbLogs.append( e.getMessage(  ) );

        if ( e.getCause(  ) != null )
        {
            _sbLogs.append( " : " );
            _sbLogs.append( e.getCause(  ).getMessage(  ) );
        }

        if ( StringUtils.isNotBlank( strMessage ) )
        {
            _sbLogs.append( " - " + strMessage );
        }

        _sbLogs.append( "</strong>\r\n" );
        AppLogService.error( "Indexing error : " + e.getMessage(  ), e );
    }

    /**
     * Log the error for the indexer action
     * @param action the {@link IndexerAction}
     * @param e the exception
     */
    public static void error( IndexerAction action, Exception e, String strMessage )
    {
        _sbLogs.append( "<strong class=\"alert\">Action from indexer : " );
        _sbLogs.append( action.getIndexerName(  ) );
        _sbLogs.append( " Action ID : " + action.getIdAction(  ) + " - Document ID : " + action.getIdDocument(  ) );
        _sbLogs.append( " - ERROR : " );
        _sbLogs.append( e.getMessage(  ) );

        if ( e.getCause(  ) != null )
        {
            _sbLogs.append( " : " );
            _sbLogs.append( e.getCause(  ).getMessage(  ) );
        }

        if ( StringUtils.isNotBlank( strMessage ) )
        {
            _sbLogs.append( " - " + strMessage );
        }

        _sbLogs.append( "</strong>\r\n" );
        AppLogService.error( "Indexing error : " + e.getMessage(  ), e );
    }

    /**
     * Gets the current index
     * @return The index
     * @deprecated use getDirectoryIndex(  ) instead
     */
    @Deprecated
    public static String getIndex(  )
    {
        return _strIndex;
    }

    /**
     * Gets the current IndexSearcher
     * @return IndexSearcher
     * @throws IOException
     */
    public static Directory getDirectoryIndex(  ) throws IOException
    {
        return NIOFSDirectory.open( new File( _strIndex ) );
    }

    /**
     * Gets the current analyser
     * @return The analyser
     */
    public static Analyzer getAnalyser(  )
    {
        return _analyzer;
    }

    /**
     * Returns all search indexers
     * @return A collection of indexers
     */
    public static Collection<SearchIndexer> getIndexers(  )
    {
        return _mapIndexers.values(  );
    }

    /**
     * return a list of IndexerAction by task key
     * @param nIdTask the task kety
     * @return a list of IndexerAction
     */
    public static List<IndexerAction> getAllIndexerActionByTask( int nIdTask )
    {
        IndexerActionFilter filter = new IndexerActionFilter(  );
        filter.setIdTask( nIdTask );

        return IndexerActionHome.getList( filter );
    }

    /**
     * Remove a Indexer Action
     * @param nIdAction the key of the action to remove
     *
     */
    public static void removeIndexerAction( int nIdAction )
    {
        IndexerActionHome.remove( nIdAction );
    }

    /**
     * Remove all Indexer Action
     *
     */
    public static void removeAllIndexerAction(  )
    {
        IndexerActionHome.removeAll(  );
    }

    /**
     * Add Indexer Action  to perform on a record
     * @param strIdDocument the id of the document
     * @param indexerName the name of the indexer
     * @param nIdTask the key of the action to do
     * @param nIdPortlet id of the portlet
     */
    public static void addIndexerAction( String strIdDocument, String indexerName, int nIdTask, int nIdPortlet )
    {
        IndexerAction indexerAction = new IndexerAction(  );
        indexerAction.setIdDocument( strIdDocument );
        indexerAction.setIdTask( nIdTask );
        indexerAction.setIndexerName( indexerName );
        indexerAction.setIdPortlet( nIdPortlet );
        IndexerActionHome.create( indexerAction );
    }

    /**
     * Add Indexer Action  to perform on a record
     * @param strIdDocument the id of the document
     * @param indexerName the name of the indexer
     * @param nIdTask the key of the action to do
     */
    public static void addIndexerAction( String strIdDocument, String indexerName, int nIdTask )
    {
        addIndexerAction( strIdDocument, indexerName, nIdTask, ALL_DOCUMENT );
    }
}
