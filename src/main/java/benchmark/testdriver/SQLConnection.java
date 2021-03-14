package benchmark.testdriver;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import benchmark.qualification.QueryResult;

import java.sql.*;

public class SQLConnection implements ServerConnection {
	private Statement statement;
	private Connection conn;

	private static Logger logger = Logger.getLogger( SQLConnection.class );
	
	public SQLConnection(String serviceURL, int timeout, String driverClassName) {
		try {
			Class.forName(driverClassName);
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		try {
			conn = DriverManager.getConnection(serviceURL);
			statement = conn.createStatement();
			
			statement.setQueryTimeout(timeout/1000);
			statement.setFetchSize(TestDriverDefaultValues.fetchSize);
		} catch(SQLException e) {
			while(e!=null) {
				e.printStackTrace();
				e=e.getNextException();
			}
			System.exit(-1);
		}
	}
	
	/*
	 * Execute Query with Query Object
	 */
	public void executeQuery(Query query, byte queryType) {
		executeQuery(query.getQueryString(), queryType, query.getNr(), query.getQueryMix());
	}

	private void createFullResultString(StringBuffer resultString, ResultSet results) throws SQLException {
		for(int i = 1; i <= results.getMetaData().getColumnCount(); i++){
			String colLabel = results.getMetaData().getColumnLabel(i);
			
			String value  = results.getString(i);
			value = value != null 
				? value.substring(0, Math.min(20, results.getString(i).length()))
				: null;
			
			resultString.append(colLabel);
			resultString.append(": ");
			resultString.append(value);
			resultString.append(" **");			
		  }

		  resultString.append("\n");
	}
	
	/*
	 * execute Query with Query String
	 */
	private void executeQuery(String queryString, byte queryType, int queryNr, QueryMix queryMix) {
		double timeInSeconds;
		
		try {
			long start = System.nanoTime();
			ResultSet results = statement.executeQuery(queryString);
	
			int resultCount = 0;
			StringBuffer resultString = new StringBuffer(); 
			while(results.next()){
				resultCount++;
				if(logger.isEnabledFor(Level.ALL)) {
					createFullResultString(resultString, results);
				}
			}
			Long stop = System.nanoTime();
			Long interval = stop-start;
			
			timeInSeconds = interval.doubleValue()/1000000000;

			int queryMixRun = queryMix.getRun() + 1;
			
			if(logger.isEnabledFor( Level.ALL ) && queryType!=3 && queryMixRun > 0)
				logResultInfo(queryNr, queryMixRun, timeInSeconds,
		                   queryString, queryType, 0,
		                   resultCount, resultString.toString());

			queryMix.setCurrent(resultCount, timeInSeconds);
			results.close();
		} catch(SQLException e) {
			while(e!=null) {
				e.printStackTrace();
				e=e.getNextException();
			}
			System.err.println("\n\nError for Query " + queryNr + ":\n\n" + queryString);
			System.exit(-1);
		}
	}
	
	/*
	 * Execute Query with precompiled Query
	 * @see benchmark.testdriver.ServerConnection#executeQuery(benchmark.testdriver.CompiledQuery, benchmark.testdriver.CompiledQueryMix)
	 */
	public void executeQuery(CompiledQuery query, CompiledQueryMix queryMix) {
		double timeInSeconds;
		String queryString = query.getQueryString();
		byte queryType = query.getQueryType();
		int queryNr = query.getNr();
		
		try {
			long start = System.nanoTime();
			ResultSet results = statement.executeQuery(queryString);
	
			int resultCount = 0;
			StringBuffer resultString = new StringBuffer(); 

			while(results.next()){
				resultCount++;
				if(logger.isEnabledFor(Level.ALL)) {
					createFullResultString(resultString, results);
				}
			}
			
			Long stop = System.nanoTime();
			Long interval = stop-start;

			timeInSeconds = interval.doubleValue()/1000000000;

			int queryMixRun = queryMix.getRun() + 1;
			
			if(logger.isEnabledFor( Level.ALL ) && queryType!=3 && queryMixRun > 0)
				logResultInfo(queryNr, queryMixRun, timeInSeconds,
		                   queryString, queryType, 0,
		                   resultCount,resultString.toString());

			queryMix.setCurrent(resultCount, timeInSeconds);
			results.close();
		} catch(SQLException e) {
			while(e!=null) {
				e.printStackTrace();
				e=e.getNextException();
			}
			System.err.println("\n\nError for Query " + queryNr + ":\n\n" + queryString);
			System.exit(-1);
		}
	}
	
	private void logResultInfo(int queryNr, int queryMixRun, double timeInSeconds,
			                   String queryString, byte queryType, int resultSizeInBytes,
			                   int resultCount, String result) {
		StringBuffer sb = new StringBuffer(1000);
		sb.append("\n\n\tQuery " + queryNr + " of run " + queryMixRun + " has been executed ");
		sb.append("in " + String.format("%.6f",timeInSeconds) + " seconds.\n" );
		sb.append("\n\tQuery string:\n\n");
		sb.append(queryString);
		sb.append("\n\n");
	
		//Log results
		if(queryType==Query.DESCRIBE_TYPE)
			sb.append("\tQuery(Describe) result (" + resultSizeInBytes + " Bytes): \n\n");
		else
			sb.append("\tQuery results (" + resultCount + " results):\n " + result + "  \n\n");
		

//		sb.append(result);
		sb.append("\n__________________________________________________________________________________\n");
		logger.log(Level.ALL, sb.toString());
	}
	
	public void close() {
		try {
		conn.close();
		} catch(SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public QueryResult executeValidation(Query query, byte queryType) {
		// TODO Auto-generated method stub
		return null;
	}
}
