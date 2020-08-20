function initializeCoreMod() {
	return {
		'setrotationangles': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.client.renderer.entity.model.BipedModel',
				'methodName': 'setRotationAngles',
				'methodDesc': '(Lnet/minecraft/entity/LivingEntity;FFFFF)V'
			},
			'transformer': function(methodNode) {
				return applySetRotationAngles(methodNode)
			}
		}, 'setrotationanglesobf': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.client.renderer.entity.model.BipedModel',
				'methodName': 'func_225597_a_',
				'methodDesc': '(Lnet/minecraft/entity/LivingEntity;FFFFF)V'
			},
			'transformer': function(methodNode) {
				return applySetRotationAngles(methodNode)
			}
		}
	}
}

function applySetRotationAngles(methodNode){
	var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI')
	var Opcodes = Java.type('org.objectweb.asm.Opcodes')
	var Label = Java.type('org.objectweb.asm.Label')
	var InsnList = Java.type('org.objectweb.asm.tree.InsnList')

	var node = ASMAPI.findFirstInstruction(methodNode, Opcodes.RETURN)
	var list = new InsnList
	do{
		methodNode.instructions.remove(node)
		list.add(node)
		node = node.getNext()
	}while(node!=null)

	methodNode.visitVarInsn(Opcodes.ALOAD, 1)
	methodNode.visitTypeInsn(Opcodes.INSTANCEOF, 'net/minecraft/entity/player/PlayerEntity')
	var l = new Label
	methodNode.visitJumpInsn(Opcodes.IFEQ, l)
	methodNode.visitVarInsn(Opcodes.ALOAD, 0)
	methodNode.visitVarInsn(Opcodes.ALOAD, 1)
	methodNode.visitTypeInsn(Opcodes.CHECKCAST, 'net/minecraft/entity/player/PlayerEntity')
	methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, 'tictim/paraglider/utils/ParagliderUtils$Client', 'setParagliderRotationAngles', '(Lnet/minecraft/client/renderer/entity/model/BipedModel;Lnet/minecraft/entity/player/PlayerEntity;)V', false)
	methodNode.visitLabel(l)
	methodNode.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
	
	methodNode.instructions.add(list)
	methodNode.visitLabel(new Label)

	return methodNode;
}