/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.db.internal.processor;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.db.internal.domain.connection.DbConnection;
import org.mule.module.db.internal.domain.executor.BulkExecutor;
import org.mule.module.db.internal.domain.executor.BulkQueryExecutorFactory;
import org.mule.module.db.internal.domain.query.Query;
import org.mule.module.db.internal.domain.query.QueryParamValue;
import org.mule.module.db.internal.domain.query.QueryType;
import org.mule.module.db.internal.domain.transaction.TransactionalAction;
import org.mule.module.db.internal.resolver.database.DbConfigResolver;
import org.mule.module.db.internal.resolver.param.DynamicParamValueResolver;
import org.mule.module.db.internal.resolver.param.ParamValueResolver;
import org.mule.module.db.internal.resolver.query.QueryResolver;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Executes an update query in bulk mode on a database
 * * <p/>
 * An update query can be parametrized update, insert or delete query or a stored procedure
 * taking input parameters only and returning an update count.
 * <p/>
 * Both database and queries are resolved, if required, using the {@link org.mule.api.MuleEvent}
 * being processed.
 */
public class PreparedBulkUpdateMessageProcessor extends AbstractBulkUpdateMessageProcessor
{

    public PreparedBulkUpdateMessageProcessor(DbConfigResolver dbConfigResolver, QueryResolver queryResolver, BulkQueryExecutorFactory bulkUpdateExecutorFactory, TransactionalAction transactionalAction, List<QueryType> validQueryTypes)
    {
        super(dbConfigResolver, transactionalAction, validQueryTypes, queryResolver, bulkUpdateExecutorFactory);
    }

    @Override
    protected Object executeQuery(DbConnection connection, MuleEvent muleEvent) throws SQLException
    {
        Query query = queryResolver.resolve(connection, muleEvent);

        validateQueryType(query.getQueryTemplate());

        List<List<QueryParamValue>> paramValues = resolveParamSets(muleEvent, query);

        BulkExecutor bulkUpdateExecutor = bulkUpdateExecutorFactory.create();
        return bulkUpdateExecutor.execute(connection, query, paramValues);
    }

    private List<List<QueryParamValue>> resolveParamSets(MuleEvent muleEvent, Query query)
    {
        final Iterator<Object> paramsIterator = getIterator(muleEvent);

        ParamValueResolver paramValueResolver = new DynamicParamValueResolver(muleContext.getExpressionManager());

        List<List<QueryParamValue>> result = new LinkedList<List<QueryParamValue>>();

        while (paramsIterator.hasNext())
        {
            MuleMessage itemMessage = new DefaultMuleMessage(paramsIterator.next(), muleContext);
            MuleEvent itemEvent = new DefaultMuleEvent(itemMessage, muleEvent);
            List<QueryParamValue> queryParamValues = paramValueResolver.resolveParams(itemEvent, query.getParamValues());
            result.add(queryParamValues);
        }

        return result;
    }

}
