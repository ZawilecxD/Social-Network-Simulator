package network;

import java.util.concurrent.atomic.AtomicInteger;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import user.Sex;
import user.User;
import user.UserCharacteristics;

public class SocialNetworkContext implements ContextBuilder<Object> {

	public AtomicInteger usersId;
	@Override
	public Context build(Context<Object> context) {
		usersId.set(0);
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("friends network", context, true);
		netBuilder.buildNetwork();
		
		Parameters params = RunEnvironment.getInstance().getParameters();

		//parameters
		int usersNumber = (Integer) params.getValue("usersNumber");
		
		for(int i=0; i<usersNumber; i++) {
			context.add(new User(usersId.getAndAdd(1), "Imie", "Nazwisko", Sex.MALE, new UserCharacteristics()));
		}
		
		return context;
	}

}
