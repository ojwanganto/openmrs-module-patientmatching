package org.regenstrief.linkage.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.regenstrief.linkage.analysis.EMAnalyzer;
import org.regenstrief.linkage.analysis.PairDataSourceAnalysis;
import org.regenstrief.linkage.analysis.RandomSampleAnalyzer;
import org.regenstrief.linkage.analysis.VectorTable;
import org.regenstrief.linkage.io.FormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;

/**
 * Class displays different analysis options available in the record linkaeg GUI
 * 
 * @author jegg
 *
 */

public class AnalysisPanel extends JPanel implements ActionListener{
	RecMatchConfig rm_conf;
	
	private JButton randomButton;
	
	private JButton the_button, vector_button;
	
	public AnalysisPanel(RecMatchConfig rmc){
		super();
		rm_conf = rmc;
		createAnalysisPanel();
	}
	
	public void setRecMatchConfig(RecMatchConfig rmc){
		rm_conf = rmc;
	}
	
	private void createAnalysisPanel(){
		//this.setLayout(new BorderLayout());
	    randomButton = new JButton("Perform Random Sampling");
        this.add(randomButton);
        randomButton.addActionListener(this);
        
		the_button = new JButton("Perform EM Analysis");
		this.add(the_button);
		the_button.addActionListener(this);
		
		vector_button = new JButton("View score tables");
		this.add(vector_button);
		vector_button.addActionListener(this);
	}
	
	private void runEMAnalysis(){
		ReaderProvider rp = new ReaderProvider();
		List<MatchingConfig> mcs = rm_conf.getMatchingConfigs();
		Iterator<MatchingConfig> it = mcs.iterator();
		while(it.hasNext()){
			MatchingConfig mc = it.next();
			
			OrderedDataSourceReader odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
			OrderedDataSourceReader odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
			if(odsr1 != null && odsr2 != null){
				// analyze with EM
				FormPairs fp2 = new FormPairs(odsr1, odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
				/*
				 * Using two analyzer at a time in the PairDataSourceAnalysis. The order when adding the analyzer to
				 * PairDataSourceAnalysis will affect the end results. For example in the following code fragment,
				 * RandomSampleAnalyzer will be run first followed by the EMAnalyzer. But this will be depend on
				 * current Java's ArrayList implementation, if they add new element add the end of the list, then
				 * this will work fine.
				 * 
				 * In the following code, RandomSampleAnalyzer and EMAnalyzer will work independent each other.
				 * RandomSampleAnalyzer will generate the u value and save it in MatchingConfigRow object, while
				 * EMAnalyzer will check MatchingConfig to find out whether the blocking run use random sampling
				 * (where u value that will be used is the one generated by RandomSampleAnalyzer) or not using 
				 * random sampling (where u value will be the default value).
				 * 
				 * I don't think we need to instantiate the RandomSampleAnalyzer here if the user doesn't want to
				 * use random sampling :D
				 */
                LoggingFrame frame = new LoggingFrame(mc.getName());
				PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);
				// if not using random sampling then don't instantiate random sample analyzer
				// if the value is locked then don't instantiate random sampler analyzer
				if(mc.isUsingRandomSampling() && !mc.isLockedUValues()) {
				    RandomSampleAnalyzer rsa = new RandomSampleAnalyzer(rm_conf.getLinkDataSource1(), rm_conf.getLinkDataSource2(), mc);
	                pdsa.addAnalyzer(rsa);
	                frame.addLoggingObject(rsa);
				}
				EMAnalyzer ema = new EMAnalyzer(rm_conf.getLinkDataSource1(), rm_conf.getLinkDataSource2(), mc);
				pdsa.addAnalyzer(ema);
				frame.addLoggingObject(ema);
				frame.configureLoggingFrame();
				pdsa.analyzeData();
			}
			odsr1.close();
			odsr2.close();
		}
	}
	
	private void displayVectorTables(){
		Iterator<MatchingConfig> it = rm_conf.getMatchingConfigs().iterator();
		while(it.hasNext()){
			MatchingConfig mc = it.next();
			VectorTable vt = new VectorTable(mc);
			TextDisplayFrame tdf = new TextDisplayFrame(mc.getName(), vt.toString());
		}
	}
	
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource() == the_button){
			runEMAnalysis();
		} else if(ae.getSource() == vector_button){
			displayVectorTables();
		} else if(ae.getSource() == randomButton) {
		    performRandomSampling();
		}
	}

    private void performRandomSampling() {
        ReaderProvider rp = new ReaderProvider();
        List<MatchingConfig> mcs = rm_conf.getMatchingConfigs();
        Iterator<MatchingConfig> it = mcs.iterator();
        while(it.hasNext()){
            MatchingConfig mc = it.next();
            // if the user not choose to use random sampling, then do nothing
            // if the u-values is already locked then do nothing as well
            if(mc.isUsingRandomSampling() && !mc.isLockedUValues()) {
                OrderedDataSourceReader odsr1 = rp.getReader(rm_conf.getLinkDataSource1(), mc);
                OrderedDataSourceReader odsr2 = rp.getReader(rm_conf.getLinkDataSource2(), mc);
                if(odsr1 != null && odsr2 != null){
                    FormPairs fp2 = new FormPairs(odsr1, odsr2, mc, rm_conf.getLinkDataSource1().getTypeTable());
                    
                    PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);
                    
                    RandomSampleLoggingFrame frame = new RandomSampleLoggingFrame(mc);
                    
                    MatchingConfig mcCopy = (MatchingConfig) mc.clone();
                    
                    RandomSampleAnalyzer rsa = new RandomSampleAnalyzer(rm_conf.getLinkDataSource1(), rm_conf.getLinkDataSource2(), mcCopy);
                    
                    pdsa.addAnalyzer(rsa);
                    frame.addLoggingObject(rsa);
                    
                    frame.configureLoggingFrame();
                    pdsa.analyzeData();
                }
                odsr1.close();
                odsr2.close();
            }
        }
    }
}
