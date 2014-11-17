/*
 * Copyright 2009-2013 by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.asterix.aql.expression;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.asterix.aql.base.Statement;
import edu.uci.ics.asterix.aql.expression.visitor.IAqlExpressionVisitor;
import edu.uci.ics.asterix.aql.expression.visitor.IAqlVisitorWithVoidReturn;
import edu.uci.ics.asterix.common.config.DatasetConfig.IndexType;
import edu.uci.ics.asterix.common.exceptions.AsterixException;

public class CreateIndexStatement implements Statement {

    private Identifier indexName;
    private Identifier dataverseName;
    private Identifier datasetName;
    private List<String> fieldExprs = new ArrayList<String>();
    private IndexType indexType = IndexType.BTREE;
    private boolean ifNotExists;

    // Specific to NGram indexes.
    private int gramLength;

    // Specific to SIF indexes
    private double bottomLeftX;
    private double bottomLeftY;
    private double topRightX;
    private double topRightY;
    private long xCellNum;
    private long yCellNum;

    public CreateIndexStatement() {
    }

    public void setGramLength(int gramLength) {
        this.gramLength = gramLength;
    }

    public int getGramLength() {
        return gramLength;
    }

    public Identifier getIndexName() {
        return indexName;
    }

    public void setIndexName(Identifier indexName) {
        this.indexName = indexName;
    }

    public Identifier getDataverseName() {
        return dataverseName;
    }

    public void setDataverseName(Identifier dataverseName) {
        this.dataverseName = dataverseName;
    }

    public Identifier getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(Identifier datasetName) {
        this.datasetName = datasetName;
    }

    public List<String> getFieldExprs() {
        return fieldExprs;
    }

    public void addFieldExpr(String fe) {
        this.fieldExprs.add(fe);
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public boolean getIfNotExists() {
        return this.ifNotExists;
    }

    @Override
    public Kind getKind() {
        return Kind.CREATE_INDEX;
    }

    @Override
    public <R, T> R accept(IAqlExpressionVisitor<R, T> visitor, T arg) throws AsterixException {
        return visitor.visitCreateIndexStatement(this, arg);
    }

    @Override
    public <T> void accept(IAqlVisitorWithVoidReturn<T> visitor, T arg) throws AsterixException {
        visitor.visit(this, arg);
    }

    public double getBottomLeftX() {
        return bottomLeftX;
    }

    public void setBottomLeftX(double bottomLeftX) {
        this.bottomLeftX = bottomLeftX;
    }

    public double getBottomLeftY() {
        return bottomLeftY;
    }

    public void setBottomLeftY(double bottomLeftY) {
        this.bottomLeftY = bottomLeftY;
    }

    public double getTopRightX() {
        return topRightX;
    }

    public void setTopRightX(double topRightX) {
        this.topRightX = topRightX;
    }

    public double getTopRightY() {
        return topRightY;
    }

    public void setTopRightY(double topRightY) {
        this.topRightY = topRightY;
    }

    public long getXCellNum() {
        return xCellNum;
    }

    public void setXCellNum(long xCellNum) {
        this.xCellNum = xCellNum;
    }

    public long getYCellNum() {
        return yCellNum;
    }

    public void setYCellNum(long yCellNum) {
        this.yCellNum = yCellNum;
    }

}
