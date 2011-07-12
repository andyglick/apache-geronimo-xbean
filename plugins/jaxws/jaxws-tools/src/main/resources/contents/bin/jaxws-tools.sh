# ! / b i n / s h 
 # 
 #     L i c e n s e d   t o   t h e   A p a c h e   S o f t w a r e   F o u n d a t i o n   ( A S F )   u n d e r   o n e   o r   m o r e 
 #     c o n t r i b u t o r   l i c e n s e   a g r e e m e n t s .     S e e   t h e   N O T I C E   f i l e   d i s t r i b u t e d   w i t h 
 #     t h i s   w o r k   f o r   a d d i t i o n a l   i n f o r m a t i o n   r e g a r d i n g   c o p y r i g h t   o w n e r s h i p . 
 #     T h e   A S F   l i c e n s e s   t h i s   f i l e   t o   Y o u   u n d e r   t h e   A p a c h e   L i c e n s e ,   V e r s i o n   2 . 0 
 #     ( t h e   " L i c e n s e " ) ;   y o u   m a y   n o t   u s e   t h i s   f i l e   e x c e p t   i n   c o m p l i a n c e   w i t h 
 #     t h e   L i c e n s e .     Y o u   m a y   o b t a i n   a   c o p y   o f   t h e   L i c e n s e   a t 
 #   
 #             h t t p : / / w w w . a p a c h e . o r g / l i c e n s e s / L I C E N S E - 2 . 0 
 #   
 #       U n l e s s   r e q u i r e d   b y   a p p l i c a b l e   l a w   o r   a g r e e d   t o   i n   w r i t i n g ,   s o f t w a r e 
 #       d i s t r i b u t e d   u n d e r   t h e   L i c e n s e   i s   d i s t r i b u t e d   o n   a n   " A S   I S "   B A S I S , 
 #       W I T H O U T   W A R R A N T I E S   O R   C O N D I T I O N S   O F   A N Y   K I N D ,   e i t h e r   e x p r e s s   o r   i m p l i e d . 
 #       S e e   t h e   L i c e n s e   f o r   t h e   s p e c i f i c   l a n g u a g e   g o v e r n i n g   p e r m i s s i o n s   a n d 
 #       l i m i t a t i o n s   u n d e r   t h e   L i c e n s e . 
 
 #   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
 #   $ R e v :   5 3 4 4 5 4   $   $ D a t e :   2 0 0 7 - 0 5 - 0 2   0 9 : 3 9 : 4 9   - 0 4 0 0   ( W e d ,   0 2   M a y   2 0 0 7 )   $ 
 #   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
 
 #   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
 #   Y o u   s h o u l d   n o t   h a v e   t o   e d i t   t h i s   f i l e .     I f   y o u   w i s h   t o   h a v e   e n v i r o n m e n t 
 #   v a r i a b l e s   s e t   e a c h   t i m e   y o u   r u n   t h i s   s c r i p t   r e f e r   t o   t h e   i n f o r m a t i o n 
 #   o n   t h e   s e t e n v . s h   s c r i p t   t h a t   i s   c a l l e d   b y   t h i s   s c r i p t   b e l o w .   
 # 
 #   I n v o c a t i o n   S y n t a x : 
 # 
 #       j a x w s - t o o l s . s h   [ g e n e r a l   o p t i o n s ]   c o m m a n d   [ c o m m a n d   o p t i o n s ]   
 # 
 #       F o r   d e t a i l e d   c o m m a n d   u s a g e   i n f o r m a t i o n ,   j u s t   r u n   d e p l o y . s h   w i t h o u t   a n y   
 #       a r g u m e n t s . 
 # 
 #   E n v i r o n m e n t   V a r i a b l e   P r e q u i s i t e s : 
 # 
 #       G E R O N I M O _ H O M E       ( O p t i o n a l )   M a y   p o i n t   a t   y o u r   G e r o n i m o   t o p - l e v e l   d i r e c t o r y . 
 #                                       I f   n o t   s p e c i f i e d ,   i t   w i l l   d e f a u l t   t o   t h e   p a r e n t   d i r e c t o r y 
 #                                       o f   t h e   l o c a t i o n   o f   t h i s   s c r i p t . 
 # 
 #       G E R O N I M O _ O P T S       ( O p t i o n a l )   J a v a   r u n t i m e   o p t i o n s . 
 # 
 #       G E R O N I M O _ T M P D I R   ( O p t i o n a l )   D i r e c t o r y   p a t h   l o c a t i o n   o f   t e m p o r a r y   d i r e c t o r y 
 #                                       t h e   J V M   s h o u l d   u s e   ( j a v a . i o . t m p d i r ) .   D e f a u l t s   t o   v a r / t e m p 
 #                                       ( r e s o l v e d   t o   s e r v e r   i n s t a n c e   d i r e c t o r y ) . 
 # 
 #       J A V A _ H O M E               P o i n t s   t o   y o u r   J a v a   D e v e l o p m e n t   K i t   i n s t a l l a t i o n . 
 #                                       J A V A _ H O M E   d o e s n ' t   n e e d   t o   b e   s e t   i f   J R E _ H O M E   i s   s e t . 
 #                                       I t   i s   m a n d a t o r y   e i t h e r   J A V A _ H O M E   o r   J R E _ H O M E   a r e   s e t . 
 # 
 #       J R E _ H O M E                 P o i n t s   t o   y o u r   J a v a   R u n t i m e   E n v i r o n m e n t   i n s t a l l a t i o n . 
 #                                       S e t   t h i s   i f   y o u   w i s h   t o   r u n   G e r o n i m o   u s i n g   t h e   J R E   
 #                                       i n s t e a d   o f   t h e   J D K .   D e f a u l t s   t o   J A V A _ H O M E   i f   e m p t y . 
 #                                       I t   i s   m a n d a t o r y   e i t h e r   J A V A _ H O M E   o r   J R E _ H O M E   a r e   s e t . 
 # 
 #       J A V A _ O P T S               ( O p t i o n a l )   J a v a   r u n t i m e   o p t i o n s . 
 # 
 #   T r o u b l e s h o o t i n g   e x e c u t i o n   o f   t h i s   s c r i p t   f i l e : 
 # 
 #     G E R O N I M O _ E N V _ I N F O         ( O p t i o n a l )   E n v i r o n m e n t   v a r i a b l e   t h a t   w h e n   s e t   t o 
 #                                               " o n "   ( t h e   d e f a u l t )   o u t p u t s   t h e   
 #                                               v a l u e s   o f   G E R O N I M O _ H O M E ,   
 #                                               G E R O N I M O _ T M P D I R ,   J A V A _ H O M E ,   J R E _ H O M E   b e f o r e 
 #                                               t h e   c o m m a n d   i s   i s s u e d .   S e t   t o   " o f f "   i f   y o u 
 #                                               d o   w a n t   t o   s e e   t h i s   i n f o r m a t i o n . 
 # 
 #   S c r i p t s   c a l l e d   b y   t h i s   s c r i p t : 
 #   
 #       $ G E R O N I M O _ H O M E / b i n / s e t e n v . s h 
 #                                       ( O p t i o n a l )   T h i s   s c r i p t   f i l e   i s   c a l l e d   i f   i t   i s   p r e s e n t . 
 #                                       I t s   c o n t e n t s   m a y   s e t   o n e   o r   m o r e   o f   t h e   a b o v e   e n v i r o n m e n t 
 #                                       v a r i a b l e s .     I t   i s   p r e f e r a b l e   ( t o   s i m p l i f y   m i g r a t i o n   t o 
 #                                       f u t u r e   G e r o n i m o   r e l e a s e s )   t o   s e t   e n v i r o n m e n t   v a r i a b l e s 
 #                                       i n   t h i s   f i l e   r a t h e r   t h a n   m o d i f y i n g   G e r o n i m o ' s   s c r i p t   f i l e s . 
 # 
 #       $ G E R O N I M O _ H O M E / b i n / s e t j a v a e n v . s h 
 #                                       T h i s   b a t c h   f i l e   i s   c a l l e d   t o   s e t   e n v i r o n m e n t   v a r i a b l e s 
 #                                       r e l a t i n g   t o   t h e   j a v a   o r   j d b   e x e c u t a b l e   t o   i n v o k e . 
 #                                       T h i s   f i l e   s h o u l d   n o t   n e e d   t o   b e   m o d i f i e d . 
 # 
 #   E x i t   C o d e s : 
 # 
 #     0   -   S u c c e s s 
 #     1   -   E r r o r 
 #   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
 
 #   O S   s p e c i f i c   s u p p o r t .     $ v a r   _ m u s t _   b e   s e t   t o   e i t h e r   t r u e   o r   f a l s e . 
 c y g w i n = f a l s e 
 o s 4 0 0 = f a l s e 
 c a s e   " ` u n a m e ` "   i n 
 C Y G W I N * )   c y g w i n = t r u e ; ; 
 O S 4 0 0 * )   o s 4 0 0 = t r u e ; ; 
 e s a c 
 
 #   r e s o l v e   l i n k s   -   $ 0   m a y   b e   a   s o f t l i n k 
 P R G = " $ 0 " 
 
 w h i l e   [   - h   " $ P R G "   ] ;   d o 
     l s = ` l s   - l d   " $ P R G " ` 
     l i n k = ` e x p r   " $ l s "   :   ' . * - >   \ ( . * \ ) $ ' ` 
     i f   e x p r   " $ l i n k "   :   ' / . * '   >   / d e v / n u l l ;   t h e n 
         P R G = " $ l i n k " 
     e l s e 
         P R G = ` d i r n a m e   " $ P R G " ` / " $ l i n k " 
     f i 
 d o n e 
 
 #   G e t   s t a n d a r d   e n v i r o n m e n t   v a r i a b l e s 
 P R G D I R = ` d i r n a m e   " $ P R G " ` 
 
 #   O n l y   s e t   G E R O N I M O _ H O M E   i f   n o t   a l r e a d y   s e t 
 [   - z   " $ G E R O N I M O _ H O M E "   ]   & &   G E R O N I M O _ H O M E = ` c d   " $ P R G D I R / . . "   ;   p w d ` 
 
 i f   [   - r   " $ G E R O N I M O _ H O M E " / b i n / s e t e n v . s h   ] ;   t h e n 
     .   " $ G E R O N I M O _ H O M E " / b i n / s e t e n v . s h 
 f i 
 
 #   F o r   C y g w i n ,   e n s u r e   p a t h s   a r e   i n   U N I X   f o r m a t   b e f o r e   a n y t h i n g   i s   t o u c h e d 
 i f   $ c y g w i n ;   t h e n 
     [   - n   " $ J A V A _ H O M E "   ]   & &   J A V A _ H O M E = ` c y g p a t h   - - u n i x   " $ J A V A _ H O M E " ` 
     [   - n   " $ J R E _ H O M E "   ]   & &   J R E _ H O M E = ` c y g p a t h   - - u n i x   " $ J R E _ H O M E " ` 
     [   - n   " $ G E R O N I M O _ H O M E "   ]   & &   G E R O N I M O _ H O M E = ` c y g p a t h   - - u n i x   " $ G E R O N I M O _ H O M E " ` 
 f i 
 
 #   F o r   O S 4 0 0 
 i f   $ o s 4 0 0 ;   t h e n 
     #   S e t   j o b   p r i o r i t y   t o   s t a n d a r d   f o r   i n t e r a c t i v e   ( i n t e r a c t i v e   -   6 )   b y   u s i n g 
     #   t h e   i n t e r a c t i v e   p r i o r i t y   -   6 ,   t h e   h e l p e r   t h r e a d s   t h a t   r e s p o n d   t o   r e q u e s t s 
     #   w i l l   b e   r u n n i n g   a t   t h e   s a m e   p r i o r i t y   a s   i n t e r a c t i v e   j o b s . 
     C O M M A N D = ' c h g j o b   j o b ( ' $ J O B N A M E ' )   r u n p t y ( 6 ) ' 
     s y s t e m   $ C O M M A N D 
 
     #   E n a b l e   m u l t i   t h r e a d i n g 
     e x p o r t   Q I B M _ M U L T I _ T H R E A D E D = Y 
 f i 
 
 #   G e t   s t a n d a r d   J a v a   e n v i r o n m e n t   v a r i a b l e s 
 #   ( b a s e d   u p o n   T o m c a t ' s   s e t c l a s s p a t h . s h   b u t   r e n a m e d   s i n c e   G e r o n i m o ' s   c l a s s p a t h   
 #   i s   s e t   i n   t h e   J A R   m a n i f e s t ) 
 i f   $ o s 4 0 0 ;   t h e n 
     #   - r   w i l l   O n l y   w o r k   o n   t h e   o s 4 0 0   i f   t h e   f i l e s   a r e : 
     #   1 .   o w n e d   b y   t h e   u s e r 
     #   2 .   o w n e d   b y   t h e   P R I M A R Y   g r o u p   o f   t h e   u s e r 
     #   t h i s   w i l l   n o t   w o r k   i f   t h e   u s e r   b e l o n g s   i n   s e c o n d a r y   g r o u p s 
     B A S E D I R = " $ G E R O N I M O _ H O M E " 
     .   " $ G E R O N I M O _ H O M E " / b i n / s e t j a v a e n v . s h   
 e l s e 
     i f   [   - r   " $ G E R O N I M O _ H O M E " / b i n / s e t j a v a e n v . s h   ] ;   t h e n 
         B A S E D I R = " $ G E R O N I M O _ H O M E " 
         .   " $ G E R O N I M O _ H O M E " / b i n / s e t j a v a e n v . s h 
     e l s e 
         e c h o   " C a n n o t   f i n d   $ G E R O N I M O _ H O M E / b i n / s e t j a v a e n v . s h " 
         e c h o   " T h i s   f i l e   i s   n e e d e d   t o   r u n   t h i s   p r o g r a m " 
         e x i t   1 
     f i 
 f i 
 
 i f   [   - z   " $ G E R O N I M O _ T M P D I R "   ]   ;   t h e n 
     #   D e f i n e   t h e   j a v a . i o . t m p d i r   t o   u s e   f o r   G e r o n i m o 
     G E R O N I M O _ T M P D I R = v a r / t e m p 
 f i 
 
 #   F o r   C y g w i n ,   s w i t c h   p a t h s   t o   W i n d o w s   f o r m a t   b e f o r e   r u n n i n g   j a v a 
 i f   $ c y g w i n ;   t h e n 
     J A V A _ H O M E = ` c y g p a t h   - - a b s o l u t e   - - w i n d o w s   " $ J A V A _ H O M E " ` 
     J R E _ H O M E = ` c y g p a t h   - - a b s o l u t e   - - w i n d o w s   " $ J R E _ H O M E " ` 
     G E R O N I M O _ H O M E = ` c y g p a t h   - - a b s o l u t e   - - w i n d o w s   " $ G E R O N I M O _ H O M E " ` 
     G E R O N I M O _ T M P D I R = ` c y g p a t h   - - a b s o l u t e   - - w i n d o w s   " $ G E R O N I M O _ T M P D I R " ` 
 f i 
 
 #   - - - - -   E x e c u t e   T h e   R e q u e s t e d   C o m m a n d   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
 i f   [   " $ G E R O N I M O _ E N V _ I N F O "   ! =   " o f f "   ]   ;   t h e n 
     e c h o   " U s i n g   G E R O N I M O _ H O M E :       $ G E R O N I M O _ H O M E " 
     e c h o   " U s i n g   G E R O N I M O _ T M P D I R :   $ G E R O N I M O _ T M P D I R " 
     i f   [   " $ 1 "   =   " d e b u g "   ]   ;   t h e n 
         e c h o   " U s i n g   J A V A _ H O M E :               $ J A V A _ H O M E " 
         e c h o   " U s i n g   J D B _ S R C P A T H :           $ J D B _ S R C P A T H " 
     e l s e 
         e c h o   " U s i n g   J R E _ H O M E :                 $ J R E _ H O M E " 
     f i 
 f i 
 
 e x e c   " $ _ R U N J A V A "   $ J A V A _ O P T S   $ G E R O N I M O _ O P T S   \ 
     - D o r g . a p a c h e . g e r o n i m o . h o m e . d i r = " $ G E R O N I M O _ H O M E "   \ 
     - D j a v a . i o . t m p d i r = " $ G E R O N I M O _ T M P D I R "   \ 
     - j a r   " $ G E R O N I M O _ H O M E " / b i n / j a x w s - t o o l s . j a r   " $ @ "   
 