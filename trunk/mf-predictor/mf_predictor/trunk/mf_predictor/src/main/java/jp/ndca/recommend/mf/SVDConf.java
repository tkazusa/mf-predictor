package jp.ndca.recommend.mf;

public class SVDConf {

	private static int DEFAULT_K = 10;
	private static int DEFAULT_MAX_LOOP = 20;
	private static double DEFAULT_GAMMA = 0.02;
	private static double DEFAULT_LAMBDA = 0.015;
	private static double DEFAULT_LAMBDA2 = 0.012;
	private static double DEFAULT_CONVERGENCE_RATE = 0.01;
	private static double DEFAULT_INIT_VAL = 0.1;
	
	protected int k = DEFAULT_K;
	protected int maxLoop = DEFAULT_MAX_LOOP;
	protected double gamma = DEFAULT_GAMMA;
	protected double lambda = DEFAULT_LAMBDA;
	protected double lambda2 = DEFAULT_LAMBDA2;
	protected double initVal = DEFAULT_INIT_VAL;
	protected double convergenceRate = DEFAULT_CONVERGENCE_RATE;
	protected boolean testConvergence = false;

	public int getK()				{		return k;				}
	public int getMaxLoop()		{		return maxLoop;		}
	public double getGamma()		{		return gamma;			}
	public double getLambda() 	{		return lambda;			}
	public double getLambda2()	{		return lambda2;		}
	public double getInitVal()	{		return initVal;		}
	public double getConvergenceRate()
		{		return convergenceRate;		}
	public boolean isTestConvergence()
		{		return testConvergence;		}
	
	public void setK(int k)
	{			this.k = k;							}
	public void setMaxLoop(int maxLoop)
	{			this.maxLoop = maxLoop;				}
	public void setGamma(double gamma)
	{			this.gamma = gamma;					}
	public void setLambda(double lambda)
	{			this.lambda = lambda;				}
	public void setLambda2(double lambda2)
	{			this.lambda2 = lambda2;				}
	public void setInitVal(double initVal)
	{			this.initVal = initVal;				}
	public void setConvergenceRate(double convergenceRate)
	{		this.convergenceRate = convergenceRate;			}
	public void setTestConvergence(boolean testConvergence)
	{		this.testConvergence = testConvergence;			}

}
