
/* First created by JCasGen Wed Jan 13 16:58:25 CET 2016 */
package de.unidue.ltl.tok.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Wed Mar 30 09:47:09 CEST 2016
 * @generated */
public class ExToken_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ExToken_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ExToken_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ExToken(addr, ExToken_Type.this);
  			   ExToken_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ExToken(addr, ExToken_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ExToken.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.unidue.ltl.tokenizer.type.ExToken");
 
  /** @generated */
  final Feature casFeat_tokenValue;
  /** @generated */
  final int     casFeatCode_tokenValue;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTokenValue(int addr) {
        if (featOkTst && casFeat_tokenValue == null)
      jcas.throwFeatMissing("tokenValue", "de.unidue.ltl.tokenizer.type.ExToken");
    return ll_cas.ll_getStringValue(addr, casFeatCode_tokenValue);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTokenValue(int addr, String v) {
        if (featOkTst && casFeat_tokenValue == null)
      jcas.throwFeatMissing("tokenValue", "de.unidue.ltl.tokenizer.type.ExToken");
    ll_cas.ll_setStringValue(addr, casFeatCode_tokenValue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ExToken_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_tokenValue = jcas.getRequiredFeatureDE(casType, "tokenValue", "uima.cas.String", featOkTst);
    casFeatCode_tokenValue  = (null == casFeat_tokenValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_tokenValue).getCode();

  }
}



    