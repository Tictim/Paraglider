package tictim.paraglider.contents;

import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import tictim.paraglider.ModCfg;

import javax.annotation.Nullable;

public class WindEntity extends Entity{
	private BlockPos blockPos = null;
	private int lastUsedTickCount = 0;
	private static final DataParameter<Float> HEIGHT = EntityDataManager.createKey(WindEntity.class, DataSerializers.FLOAT);

	public WindEntity(World world){
		this(Contents.WIND.get(), world);
	}
	public WindEntity(EntityType<?> type, World world){
		super(type, world);
		this.noClip = true;

		this.dataManager.register(HEIGHT, 0f);
	}

	public void extendLife(){
		if(blockPos!=null) lastUsedTickCount = ticksExisted;
	}

	@Override
	protected void registerData(){}

	@Override public EntitySize getSize(Pose poseIn){
		return getType().getSize().scale(1, dataManager.get(HEIGHT));
	}
	public void setHeight(float height){
		dataManager.set(HEIGHT, height);
	}
	public void setBlockPos(@Nullable BlockPos pos){
		if(pos!=null){
			this.setPosition(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);
			this.blockPos = pos.toImmutable();
		}else this.blockPos = null;
	}

	@Nullable
	public BlockPos getBlockPos(){
		return blockPos;
	}

	@Override
	public PushReaction getPushReaction(){
		return PushReaction.IGNORE;
	}

	@Override
	public IPacket<?> createSpawnPacket(){
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick(){
		super.tick();
		if(world.isRemote){
			if(rand.nextInt(6)==0) world.addOptionalParticle(ParticleTypes.FIREWORK, getPosX()+rand.nextDouble()-0.5, getPosY()+0.5, getPosZ()+rand.nextDouble()-0.5, 0, 1, 0);
		}else{
			if(ticksExisted-lastUsedTickCount >= 200||(blockPos!=null&&!ModCfg.isWindSource(world.getBlockState(blockPos)))) this.remove();
		}
	}

	@Override public void notifyDataManagerChange(DataParameter<?> key){
		super.notifyDataManagerChange(key);
		if(key==HEIGHT) recalculateSize();
	}
	@Override
	protected void readAdditional(CompoundNBT nbt){
		if(nbt.contains("pos", 10)){
			this.blockPos = NBTUtil.readBlockPos(nbt.getCompound("pos"));
			this.lastUsedTickCount = nbt.getInt("lastUsed");
			dataManager.set(HEIGHT, nbt.getFloat("height"));
		}
	}

	@Override
	protected void writeAdditional(CompoundNBT nbt){
		if(this.blockPos!=null){
			nbt.put("pos", NBTUtil.writeBlockPos(this.blockPos));
			nbt.putInt("lastUsed", lastUsedTickCount);
			nbt.putFloat("height", dataManager.get(HEIGHT));
		}
	}
}
