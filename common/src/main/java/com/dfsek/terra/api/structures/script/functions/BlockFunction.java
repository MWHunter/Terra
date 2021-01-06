package com.dfsek.terra.api.structures.script.functions;

import com.dfsek.terra.api.math.vector.Vector2;
import com.dfsek.terra.api.math.vector.Vector3;
import com.dfsek.terra.api.platform.TerraPlugin;
import com.dfsek.terra.api.platform.block.BlockData;
import com.dfsek.terra.api.structures.parser.exceptions.ParseException;
import com.dfsek.terra.api.structures.parser.lang.ImplementationArguments;
import com.dfsek.terra.api.structures.parser.lang.Returnable;
import com.dfsek.terra.api.structures.parser.lang.constants.ConstantExpression;
import com.dfsek.terra.api.structures.parser.lang.functions.Function;
import com.dfsek.terra.api.structures.script.TerraImplementationArguments;
import com.dfsek.terra.api.structures.structure.RotationUtil;
import com.dfsek.terra.api.structures.structure.buffer.items.BufferedBlock;
import com.dfsek.terra.api.structures.tokenizer.Position;
import net.jafama.FastMath;

public class BlockFunction implements Function<Void> {
    private final BlockData data;
    private final Returnable<Number> x, y, z;
    private final Position position;
    private final Returnable<Boolean> overwrite;

    public BlockFunction(Returnable<Number> x, Returnable<Number> y, Returnable<Number> z, Returnable<String> data, Returnable<Boolean> overwrite, TerraPlugin main, Position position) throws ParseException {
        this.position = position;
        if(!(data instanceof ConstantExpression)) throw new ParseException("Block data must be constant", data.getPosition());

        this.data = main.getWorldHandle().createBlockData(((ConstantExpression<String>) data).getConstant());
        this.x = x;
        this.y = y;
        this.z = z;
        this.overwrite = overwrite;
    }

    @Override
    public Void apply(ImplementationArguments implementationArguments) {
        TerraImplementationArguments arguments = (TerraImplementationArguments) implementationArguments;
        BlockData rot = data.clone();

        Vector2 xz = new Vector2(x.apply(implementationArguments).doubleValue(), z.apply(implementationArguments).doubleValue());

        RotationUtil.rotateVector(xz, arguments.getRotation());

        RotationUtil.rotateBlockData(rot, arguments.getRotation().inverse());
        arguments.getBuffer().addItem(new BufferedBlock(rot, overwrite.apply(implementationArguments)), new Vector3(FastMath.roundToInt(xz.getX()), y.apply(implementationArguments).doubleValue(), FastMath.roundToInt(xz.getZ())).toLocation(arguments.getBuffer().getOrigin().getWorld()));
        return null;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public ReturnType returnType() {
        return ReturnType.VOID;
    }
}
