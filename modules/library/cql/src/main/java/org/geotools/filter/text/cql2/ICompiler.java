package org.geotools.filter.text.cql2;

import java.util.List;

import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;

public interface ICompiler {


    /**
     * @return the compilation source
     */
    public String getSource();

    /**
     * Compiles the source string to produce a {@link Filter}. 
     * The filter result must be retrieved with {@link #getFilter()}.
     *
     * @throws CQLException
     */
    public void compileFilter()throws CQLException;

    /**
     * The resultant filter of the compilation
     * @see #compileFilter()
     * 
     * @return Filter
     * @throws CQLException
     */
    public Filter getFilter() throws CQLException;


    /**
     * Compiles the source string to produce an {@link Expression}. 
     * The resultant expression must be retrieved with {@link #getExpression()}.
     *
     * @throws CQLException
     */
    public void compileExpression()throws CQLException;
    /**
     * The resultant {@link Expression} of the compilation.
     * @see #compileExpression()
     * @return Expression
     * @throws CQLException
     */
    public Expression getExpression() throws CQLException;

    
    /**
     * Compiles the source string to produce a {@link List} of {@link Filter}. 
     * The result must be retrieved with {@link #getFilterList()()}.
     *
     * @throws CQLException
     */
    public void compileFilterList()throws CQLException;
    
    /**
     * Return the compilation result.
     * 
     * @see #compileFilterList()
     * @return List<Filter>
     * @throws CQLException
     */
    public List<Filter> getFilterList() throws CQLException;

    /**
     * Return the token presents in the position specified.
     * 
     * @param position
     * @return IToken
     */
    public IToken getTokenInPosition(int position);

}
