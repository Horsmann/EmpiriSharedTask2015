

/* First created by JCasGen Wed Jan 13 16:58:25 CET 2016 */
package de.unidue.ltl.tok.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Mar 30 09:47:09 CEST 2016
 * XML source: /Users/toobee/Documents/Eclipse/EmpiriSharedTask2015/de.unidue.ltl.empiriStTokenisation/src/main/resources/desc/type/ExToken.xml
 * @generated */
public class ExToken extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ExToken.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ExToken() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ExToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ExToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ExToken(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: tokenValue

  /** getter for tokenValue - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTokenValue() {
    if (ExToken_Type.featOkTst && ((ExToken_Type)jcasType).casFeat_tokenValue == null)
      jcasType.jcas.throwFeatMissing("tokenValue", "de.unidue.ltl.tokenizer.type.ExToken");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ExToken_Type)jcasType).casFeatCode_tokenValue);}
    
  /** setter for tokenValue - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTokenValue(String v) {
    if (ExToken_Type.featOkTst && ((ExToken_Type)jcasType).casFeat_tokenValue == null)
      jcasType.jcas.throwFeatMissing("tokenValue", "de.unidue.ltl.tokenizer.type.ExToken");
    jcasType.ll_cas.ll_setStringValue(addr, ((ExToken_Type)jcasType).casFeatCode_tokenValue, v);}    
  }

    