package party.lemons.arcaneworld.block.tilentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import party.lemons.arcaneworld.ArcaneWorld;
import party.lemons.arcaneworld.block.tilentity.render.ItemActivation;
import party.lemons.arcaneworld.crafting.ritual.RitualRegistry;
import party.lemons.arcaneworld.crafting.ritual.Ritual;
import party.lemons.arcaneworld.handler.ArcaneWorldSounds;
import party.lemons.arcaneworld.network.MessageRitualCreateUpParticle;
import party.lemons.arcaneworld.network.MessageServerActivateRitual;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Sam on 10/09/2018.
 */
public class TileEntityRitualTable extends TileEntity implements ITickable
{
	public int tickCount;
	public float pageFlip;
	public float pageFlipPrev;
	public float flipT;
	public float flipA;
	public float bookSpread;
	public float bookSpreadPrev;
	public float bookRotation;
	public float bookRotationPrev;
	public float tRot;
	private static final Random rand = new Random();
	private final ItemStackHandler inventory = new ItemStackHandler(5);
	private List<ItemActivation> itemActivations = new ArrayList<>();
	private Ritual currentRitual = RitualRegistry.EMPTY;
	private RitualState state;
	private float stateTime = 0;
	private EntityPlayer player = null;
	private ItemStack[] itemsUsed = new ItemStack[5];

	public TileEntityRitualTable()
	{
		this.state = RitualState.NONE;
	}

    public void attemptActivateRitual(EntityPlayer player)
    {
        if (!canCast(player))
            return;

        for (Ritual ritual : RitualRegistry.REGISTRY.getValuesCollection())
        {
            if (ritual.isEmpty())
                continue;

            NonNullList<ItemStack> stacks = NonNullList.withSize(5, ItemStack.EMPTY);
            for (int i = 0; i < stacks.size(); i++)
                stacks.set(i, getInventory().getStackInSlot(i));

            if (ritual.matches(stacks))
            {
                setRitual(ritual);
                setActivator(player);
                setState(RitualState.START_UP);

                ItemStack[] usedStacks = new ItemStack[5];
                for (int i = 0; i < 5; i++)
                {
                    usedStacks[i] = getInventory().getStackInSlot(i).copy();
                }
                setStacks(usedStacks);

                //Take from tile entity inventory
                invslots:
                for (int i = 0; i < getInventory().getSlots(); i++)
                {
                    ItemStack itemInSlot = getInventory().getStackInSlot(i);
		            for (Ingredient ingredient : ritual.getRequiredItems())
                    {
                        if (ingredient != Ingredient.EMPTY && ingredient.apply(itemInSlot))
                        {
                            itemInSlot.shrink(ingredient.getMatchingStacks()[0].getCount());
                            continue invslots;
                        }
                    }
                }

                ArcaneWorld.NETWORK.sendToAllTracking(new MessageServerActivateRitual(ritual.getRegistryName(), getPos(), player, usedStacks), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
                break;
            }
        }
    }

	public void update()
	{
		this.bookSpreadPrev = this.bookSpread;
		this.bookRotationPrev = this.bookRotation;
		stateTime++;
		switch(state) {
			case NONE:
				moveBookNone();
				break;
			case START_UP:
				if(stateTime == 1) {
					world.playSound(null, pos, ArcaneWorldSounds.RITUAL_CHARGE, SoundCategory.BLOCKS, 3F, 0.5F + (rand.nextFloat() / 2));
					world.playSound(null, pos, ArcaneWorldSounds.RITUAL_START, SoundCategory.BLOCKS, 3F, 0.5F + (rand.nextFloat() / 2));
				}

				moveBookRotation(1F, 0.01F);
				particlesStartUp();

				if(stateTime == 10)
					world.playSound(null, pos, ArcaneWorldSounds.RITUAL_OUT, SoundCategory.BLOCKS, 3F, 0.5F + (rand.nextFloat() / 2));

				if (stateTime > 25) {
					setState(RitualState.ITEMS);
				}
				break;
			case ITEMS:
				moveBookRotation(0.2F, 0.1F);
				particlesItems();

				if (stateTime < 50 && ((int) stateTime) % 10 == 5) {
					addItemOut(itemsUsed[((int) (stateTime - 5)) / 10]);
				} else if (stateTime > 60) {
					setState(RitualState.FINISH);
					particlesActivate();
					if(!world.isRemote) {
						currentRitual.onActivate(world, pos, player, itemsUsed);
					}
				}
				break;
			case FINISH:
				moveBookRotation(1F, -0.1F);
				if(stateTime > 25)
					setState(RitualState.NONE);
				break;
			default:
				System.out.println(state);
			break;
		}

		while (this.bookRotation >= (float)Math.PI)
		{
			this.bookRotation -= ((float)Math.PI * 2F);
		}

		while (this.bookRotation < -(float)Math.PI)
		{
			this.bookRotation += ((float)Math.PI * 2F);
		}

		while (this.tRot >= (float)Math.PI)
		{
			this.tRot -= ((float)Math.PI * 2F);
		}

		while (this.tRot < -(float)Math.PI)
		{
			this.tRot += ((float)Math.PI * 2F);
		}

		float f2;

		for (f2 = this.tRot - this.bookRotation; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F))
		{
		}

		while (f2 < -(float)Math.PI)
		{
			f2 += ((float)Math.PI * 2F);
		}

		this.bookRotation += f2 * 0.4F;
		this.bookSpread = MathHelper.clamp(this.bookSpread, 0.0F, 1.0F);
		++this.tickCount;
		this.pageFlipPrev = this.pageFlip;
		float f = (this.flipT - this.pageFlip) * 0.4F;
		float f3 = 0.2F;
		f = MathHelper.clamp(f, -f3, f3);
		this.flipA += (f - this.flipA) * 0.9F;
		this.pageFlip += this.flipA;
	}

	private void particlesActivate()
	{
		if(world.isRemote)
			return;

		WorldServer worldServer = (WorldServer) world;
		worldServer.spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 50, 0, -0.5, 0, 1F);
	}

	private void addItemOut(ItemStack stack)
	{
		if(stack.isEmpty())
			return;

		itemActivations.add(new ItemActivation(stack));
        world.playSound(null, pos, ArcaneWorldSounds.RITUAL_ITEM, SoundCategory.BLOCKS, 1F, 0.5F + (rand.nextFloat() / 2));
    }

	private void particlesStartUp()
	{
		if(world.isRemote)
			return;

		WorldServer worldServer = (WorldServer) world;
		worldServer.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 15, 0, -0.5, 0, 1F);
	}

	private void particlesItems()
	{
		if(world.isRemote)
			return;

		WorldServer worldServer = (WorldServer) world;
		worldServer.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5F, pos.getY() + 0.7F, pos.getZ() + 0.5F, 1, 0, -0.5, 0, 0.1F);

		ArcaneWorld.NETWORK.sendToAllTracking(new MessageRitualCreateUpParticle(pos), new NetworkRegistry.TargetPoint(worldServer.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32));
	}

	private void moveBookRotation(float rotation, float spread)
	{
		this.tRot += rotation;
		this.bookSpread += spread;
	}

	private void moveBookNone()
	{
		EntityPlayer entityplayer = this.world.getClosestPlayer((double)((float)this.pos.getX() + 0.5F), (double)((float)this.pos.getY() + 0.5F), (double)((float)this.pos.getZ() + 0.5F), 3.0D, false);

		if (entityplayer != null)
		{
			double d0 = entityplayer.posX - (double)((float)this.pos.getX() + 0.5F);
			double d1 = entityplayer.posZ - (double)((float)this.pos.getZ() + 0.5F);
			this.tRot = (float) MathHelper.atan2(d1, d0);
			this.bookSpread += 0.1F;

			if (this.bookSpread < 0.5F || rand.nextInt(40) == 0)
			{
				float f1 = this.flipT;

                do
                {
                    this.flipT += (float) (rand.nextInt(4) - rand.nextInt(4));

                } while (!(f1 != this.flipT));
			}
		}
		else
		{
			this.tRot += 0.02F;
			this.bookSpread -= 0.1F;
		}
	}

	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		AxisAlignedBB bb = null;
		if(getState() != RitualState.NONE)
		{
			bb = INFINITE_EXTENT_AABB;
		}
		else
		{
			bb = new AxisAlignedBB(pos, pos.add(1, 1, 1));
		}


		return bb;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound.setTag("items", inventory.serializeNBT());

		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		if (compound.hasKey("items")) {
			inventory.deserializeNBT((NBTTagCompound) compound.getTag("items"));
		}
		super.readFromNBT(compound);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T) inventory;

		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;

		return super.hasCapability(capability, facing);
	}

	public ItemStackHandler getInventory()
	{
		return inventory;
	}

	public RitualState getState()
	{
		return state;
	}

	public void setState(RitualState state)
	{
		this.state = state;
		stateTime = 0;
	}

	public List<ItemActivation> getItemActivations() {
		return itemActivations;
	}

	public void setRitual(Ritual ritual)
	{
		this.currentRitual = ritual;
	}

	public void setActivator(EntityPlayer player)
	{
		this.player = player;
	}

	public void setStacks(ItemStack[] stacks)
	{
		this.itemsUsed = stacks;
	}

	public boolean canCast(EntityPlayer player)
    {
        if (getState() == TileEntityRitualTable.RitualState.NONE)
        {
            for (Ritual ritual : RitualRegistry.REGISTRY.getValuesCollection())
            {
                if (ritual.isEmpty())
                    continue;

                NonNullList<ItemStack> stacks = NonNullList.withSize(5, ItemStack.EMPTY);
                for (int i = 0; i < stacks.size(); i++)
                    stacks.set(i, getInventory().getStackInSlot(i));

                if (ritual.matches(stacks))
                {
                    return ritual.canCast(getWorld(), getPos(), player);
                }
            }
        }

        return false;
    }

	public  enum RitualState
	{
		NONE,
		START_UP,
		ITEMS,
		FINISH
	}
}
